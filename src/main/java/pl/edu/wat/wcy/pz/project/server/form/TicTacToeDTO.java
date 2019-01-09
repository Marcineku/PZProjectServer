package pl.edu.wat.wcy.pz.project.server.form;

import lombok.Getter;
import lombok.Setter;
import pl.edu.wat.wcy.pz.project.server.entity.game.GameType;
import pl.edu.wat.wcy.pz.project.server.entity.game.PieceCode;

@Getter
@Setter
public class TicTacToeDTO {
    private GameType gameType;
    private PieceCode pieceCode;
}