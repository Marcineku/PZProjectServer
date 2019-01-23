package pl.edu.wat.wcy.pz.project.server.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.wat.wcy.pz.project.server.entity.User;
import pl.edu.wat.wcy.pz.project.server.entity.game.GameStatus;
import pl.edu.wat.wcy.pz.project.server.entity.game.GameType;
import pl.edu.wat.wcy.pz.project.server.entity.game.TicTacToeGame;
import pl.edu.wat.wcy.pz.project.server.entity.game.TicTacToeMove;
import pl.edu.wat.wcy.pz.project.server.form.TicTacToeDTO;
import pl.edu.wat.wcy.pz.project.server.form.TicTacToeGameDTO;
import pl.edu.wat.wcy.pz.project.server.mapper.TicTacToeGameMapper;
import pl.edu.wat.wcy.pz.project.server.repository.TicTacToeGameRepository;
import pl.edu.wat.wcy.pz.project.server.repository.TicTacToeMoveRepository;
import pl.edu.wat.wcy.pz.project.server.repository.UserRepository;
import pl.edu.wat.wcy.pz.project.server.service.logic.TicTacToeLogic;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class TicTacToeService {

    private TicTacToeGameMapper ticTacToeGameMapper;

    private TicTacToeLogic ticTacToeLogic;

    private TicTacToeGameRepository ticTacToeGameRepository;
    private TicTacToeMoveRepository ticTacToeMoveRepository;

    private UserRepository userRepository;

    public TicTacToeGameDTO createGame(TicTacToeDTO ticTacToeDTO, String username) {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        if (ticTacToeGameRepository.existsByFirstPlayer_UsernameAndGameStatusIn(username, Arrays.asList(GameStatus.IN_PROGRESS, GameStatus.WAITING_FOR_PLAYER)))
            throw new RuntimeException("This player has already created a game.");

        TicTacToeGame newGame = TicTacToeGame.builder()
                .firstPlayer(user)
                .created(Calendar.getInstance().getTime())
                .firstPlayerPieceCode(ticTacToeDTO.getPieceCode())
                .gameType("singleplayer".equalsIgnoreCase(ticTacToeDTO.getGameType()) ? GameType.SINGLEPLAYER : GameType.MULTIPLAYER)
                .gameStatus(GameStatus.WAITING_FOR_PLAYER)
                .build();

        ticTacToeGameRepository.save(newGame);

        return ticTacToeGameMapper.toDto(newGame);
    }

    public TicTacToeGameDTO addSecondPlayerToGame(Long gameId, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        TicTacToeGame game = ticTacToeGameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        if (game.getSecondPlayer() != null)
            throw new RuntimeException("Somebody has already joined this game!");
        if (game.getFirstPlayer().getUsername().equals(user.getUsername()))
            throw new RuntimeException("This user is a first player");

        game.setSecondPlayer(user);
        TicTacToeGame savedGame = ticTacToeGameRepository.save(game);
        return ticTacToeGameMapper.toDto(savedGame);
    }

    public List<TicTacToeGameDTO> getAvailableGames(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        List<TicTacToeGame> games = ticTacToeGameRepository.findAllByGameTypeAndFirstPlayerNot(GameType.MULTIPLAYER, user);
        List<GameStatus> statusList = new ArrayList<>();

        statusList.add(GameStatus.WAITING_FOR_PLAYER);
        statusList.add(GameStatus.IN_PROGRESS);

        games = games.stream().filter(ticTacToeGame -> statusList.contains(ticTacToeGame.getGameStatus())).collect(Collectors.toList());
        return games.stream().map(ticTacToeGameMapper::toDto).collect(Collectors.toList());
    }

    public List<TicTacToeGame> getUserGames(String username) {
        return ticTacToeGameRepository.findAllByFirstPlayer_Username(username);
    }

    public List<TicTacToeGame> getUserGamesHistory(String username, GameType gameType) {
        if (gameType.equals(GameType.SINGLEPLAYER))
            return ticTacToeGameRepository.findAllByFirstPlayer_UsernameAndGameTypeAndGameStatusIn(username, GameType.SINGLEPLAYER, Arrays.asList(GameStatus.FIRST_PLAYER_WON, GameStatus.SECOND_PLAYER_WON, GameStatus.DRAW));
        else {
            List<TicTacToeGame> games = ticTacToeGameRepository.findAllByFirstPlayer_Username(username);
            games.addAll(ticTacToeGameRepository.findAllBySecondPlayer_Username(username));

            games = games.stream().filter(ticTacToeGame -> ticTacToeGame.getGameType().equals(GameType.MULTIPLAYER))
                    .filter(ticTacToeGame ->
                            ticTacToeGame.getGameStatus().equals(GameStatus.FIRST_PLAYER_WON) ||
                                    ticTacToeGame.getGameStatus().equals(GameStatus.SECOND_PLAYER_WON) ||
                                    ticTacToeGame.getGameStatus().equals(GameStatus.DRAW)
                    ).collect(Collectors.toList());
            return games;
        }
    }

    public List<TicTacToeMove> getGameMoves(Long gameId) {
        return ticTacToeMoveRepository.findAllByGame_GameId(gameId);
    }

    public List<TicTacToeGame> getActiveGames(String username) {
        return ticTacToeGameRepository.findAllByFirstPlayer_UsernameAndGameStatusIn(username, Arrays.asList(GameStatus.WAITING_FOR_PLAYER, GameStatus.IN_PROGRESS));
    }

    public TicTacToeGameDTO startGame(Long gameId, String username) {
        Optional<TicTacToeGame> gameOptional = ticTacToeGameRepository.findById(gameId);
        if (!gameOptional.isPresent()) {
            throw new RuntimeException("Game with id " + gameId + "not exist");
        }
        TicTacToeGame game = gameOptional.get();
        if (game.getSecondPlayer() == null) {
            throw new RuntimeException("Second player is null");
        }
        if (!username.equals(game.getFirstPlayer().getUsername())) {
            throw new RuntimeException("Only first player can start a game");
        }
        if(game.getGameStatus()!= GameStatus.WAITING_FOR_PLAYER) {
            throw new RuntimeException("Invalid game status");
        }

        game.setGameStatus(GameStatus.IN_PROGRESS);

        ticTacToeLogic.startNewGame(game);

        return ticTacToeGameMapper.toDto(game);
    }
}
