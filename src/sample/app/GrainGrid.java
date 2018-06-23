package sample.app;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import sample.controller.MainController;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by Wienio on 2018-06-05.
 */
public class GrainGrid {

    private final String pentagonalClosedName = "pentagonalClosed";
    private final String pentagonalPeriodicName = "pentagonalPeriodic";

    private Random random = new Random();
    private Cell[][] cells;
    private GraphicsContext gc;
    private int cellSize;
    private EdgeType edgeType;
    private NeighboursType neighboouhood;
    private int monteCarloIterations;

    private Map<Cell, Grain> cellsToUpdate = new HashMap<>();

    public GrainGrid(double x, double y, Canvas canvas, int cellSize, int grainAmount, String neighbourType, String edge, int radius) {
        this.cells = new Cell[(int) (x / cellSize)][(int) (y / cellSize)];
        gc = canvas.getGraphicsContext2D();
        this.cellSize = cellSize;
        this.neighboouhood = NeighboursType.getByName(neighbourType);
        this.edgeType = EdgeType.getByName(edge);

        for (int i = 0; i < cells.length; ++i) {
            for (int j = 0; j < cells[i].length; ++j) {
                cells[i][j] = new Cell();
            }
        }

        Map<Integer, Integer> helperMapForRadius = new HashMap<>();

        for (int i = 0; i < grainAmount; ++i) {
            int firstIndex = random.nextInt(cells.length);
            int secondIndex = random.nextInt(cells[0].length);

            while (!isGrainSpawnPossible(helperMapForRadius, firstIndex, secondIndex, radius)) {
                firstIndex = random.nextInt(cells.length);
                secondIndex = random.nextInt(cells[0].length);
            }

            cells[firstIndex][secondIndex] = new Cell(i + 1);
            helperMapForRadius.put(firstIndex, secondIndex);
        }
    }

    public void simulateGrowth() throws InterruptedException {
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                while (!MainController.stop) {
                    try {
                        if (edgeType == EdgeType.CLOSED) {
                            if (neighboouhood == NeighboursType.MOORE)
                                mooreClosed();
                            else if (neighboouhood == NeighboursType.VONNEUMANN)
                                vonNeumannClosed();
                            else if (neighboouhood == NeighboursType.HEXAGONAL_LEFT)
                                hexagonalLeftClosed();
                            else if (neighboouhood == NeighboursType.HEXAGONAL_RIGHT)
                                hexagonalRightClosed();
                            else if (neighboouhood == NeighboursType.PENTAGONAL_RANDOM) {
                                int randomPentagonalValue = random.nextInt(3) + 1;
                                Method method = GrainGrid.this.getClass().getMethod(pentagonalClosedName + randomPentagonalValue);
                                method.invoke(GrainGrid.this);
                            }
                        } else {
                            if (neighboouhood == NeighboursType.MOORE)
                                moorePeriodic();
                            else if (neighboouhood == NeighboursType.VONNEUMANN)
                                vonNeumannPeriodic();
                            else if (neighboouhood == NeighboursType.HEXAGONAL_LEFT)
                                hexagonalLeftPeriodic();
                            else if (neighboouhood == NeighboursType.HEXAGONAL_RIGHT)
                                hexagonalRightPeriodic();
                            else if (neighboouhood == NeighboursType.PENTAGONAL_RANDOM) {
                                int randomPentagonalValue = random.nextInt(3) + 1;
                                Method method = GrainGrid.this.getClass().getMethod(pentagonalPeriodicName + randomPentagonalValue);
                                method.invoke(GrainGrid.this);
                            }
                        }
                        if (cellsToUpdate.size() == 0)
                            MainController.stop = true;
                        updateGrid();
                        draw();
                        if (checkIfSimulationHasEnded()) {
                            break;
                        }
                    } catch (Exception e) {
                        MainController.stop = true;
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void vonNeumannClosed() {
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (cells[i][j].getState() != 0 && i != 0) {
                    if (cells[i - 1][j].getState() == 0)
                        cellsToUpdate.put(cells[i - 1][j], cells[i][j].getGrain());
                }
                if (cells[i][j].getState() != 0 && j != 0) {
                    if (cells[i][j - 1].getState() == 0)
                        cellsToUpdate.put(cells[i][j - 1], cells[i][j].getGrain());
                }
                if (cells[i][j].getState() != 0 && j != cells[i].length - 1) {
                    if (cells[i][j + 1].getState() == 0)
                        cellsToUpdate.put(cells[i][j + 1], cells[i][j].getGrain());
                }
                if (cells[i][j].getState() != 0 && i != cells.length - 1) {
                    if (cells[i + 1][j].getState() == 0)
                        cellsToUpdate.put(cells[i + 1][j], cells[i][j].getGrain());
                }
            }
        }
    }

    private void mooreClosed() {
        int startX, endX, startY, endY;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                if (cells[i][j].getState() != 0) {
                    startX = j == 0 ? j : j - 1;
                    endX = j == cells[i].length - 1 ? j : j + 1;
                    startY = i == 0 ? i : i - 1;
                    endY = i == cells.length - 1 ? i : i + 1;

                    for (int k = startY; k <= endY; ++k) {
                        for (int l = startX; l <= endX; ++l) {
                            if (cells[k][l].getState() == 0) {
                                cellsToUpdate.put(cells[k][l], cells[i][j].getGrain());
                            }
                        }
                    }
                }
            }
        }
    }

    private void moorePeriodic() {
        int startX, endX, startY, endY;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                if (cells[i][j].getState() != 0) {
                    startX = j == 0 ? j : j - 1;
                    endX = j == cells[i].length - 1 ? j : j + 1;
                    startY = i == 0 ? i : i - 1;
                    endY = i == cells.length - 1 ? i : i + 1;

                    if (i == 0) {
                        for (int l = startX; l <= endX; ++l) {
                            if (cells[cells.length - 1][l].getState() == 0) {
                                cellsToUpdate.put(cells[cells.length - 1][l], cells[i][j].getGrain());
                            }
                        }
                    }
                    if (i == cells.length - 1) {
                        for (int l = startX; l <= endX; ++l) {
                            if (cells[0][l].getState() == 0) {
                                cellsToUpdate.put(cells[0][l], cells[i][j].getGrain());
                            }
                        }
                    }

                    if (j == 0) {
                        for (int l = startY; l <= endY; ++l) {
                            if (cells[l][cells[i].length - 1].getState() == 0) {
                                cellsToUpdate.put(cells[l][cells[i].length - 1], cells[i][j].getGrain());
                            }
                        }
                    }

                    if (j == cells[i].length - 1) {
                        for (int l = startY; l <= endY; ++l) {
                            if (cells[l][0].getState() == 0) {
                                cellsToUpdate.put(cells[l][0], cells[i][j].getGrain());
                            }
                        }
                    }


                    for (int k = startY; k <= endY; ++k) {
                        for (int l = startX; l <= endX; ++l) {
                            if (cells[k][l].getState() == 0) {
                                cellsToUpdate.put(cells[k][l], cells[i][j].getGrain());
                            }
                        }
                    }
                }
            }
        }
    }

    private void vonNeumannPeriodic() {
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (cells[i][j].getState() != 0) {
                    if (i == 0) {
                        if (cells[cells.length - 1][j].getState() == 0) {
                            cellsToUpdate.put(cells[cells.length - 1][j], cells[i][j].getGrain());
                        }
                    } else if (cells[i - 1][j].getState() == 0) {
                        cellsToUpdate.put(cells[i - 1][j], cells[i][j].getGrain());
                    }

                    if (j == 0) {
                        if (cells[i][cells[i].length - 1].getState() == 0) {
                            cellsToUpdate.put(cells[i][cells[i].length - 1], cells[i][j].getGrain());
                        }
                    } else if (cells[i][j - 1].getState() == 0) {
                        cellsToUpdate.put(cells[i][j - 1], cells[i][j].getGrain());
                    }

                    if (j == cells[i].length - 1) {
                        if (cells[i][0].getState() == 0) {
                            cellsToUpdate.put(cells[i][0], cells[i][j].getGrain());
                        }
                    } else if (cells[i][j + 1].getState() == 0) {
                        cellsToUpdate.put(cells[i][j + 1], cells[i][j].getGrain());
                    }

                    if (i == cells.length - 1) {
                        if (cells[0][j].getState() == 0) {
                            cellsToUpdate.put(cells[0][j], cells[i][j].getGrain());
                        }
                    } else if (cells[i + 1][j].getState() == 0) {
                        cellsToUpdate.put(cells[i + 1][j], cells[i][j].getGrain());
                    }
                }
            }
        }
    }

    private void hexagonalLeftClosed() {
        int startX, endX, startY, endY;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (cells[i][j].getState() != 0) {
                    startX = j == 0 ? j : j - 1;
                    endX = j == cells[i].length - 1 ? j : j + 1;
                    startY = i == 0 ? i : i - 1;
                    endY = i == cells.length - 1 ? i : i + 1;

                    for (int k = startY; k <= endY; ++k) {
                        for (int l = startX; l <= endX; ++l) {
                            if (!(k == startY && l == endX) && !(k == endY && l == startX) && cells[k][l].getState() == 0) {
                                cellsToUpdate.put(cells[k][l], cells[i][j].getGrain());
                            }
                        }
                    }
                }
            }
        }
    }

    private void hexagonalLeftPeriodic() {
        int startX, endX, startY, endY;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (cells[i][j].getState() != 0) {
                    startX = j == 0 ? j : j - 1;
                    endX = j == cells[i].length - 1 ? j : j + 1;
                    startY = i == 0 ? i : i - 1;
                    endY = i == cells.length - 1 ? i : i + 1;

                    if (i == 0) {
                        for (int k = startX; k < endX; ++k) {
                            if (cells[cells.length - 1][k].getState() == 0) {
                                cellsToUpdate.put(cells[cells.length - 1][k], cells[i][j].getGrain());
                            }
                        }
                    }
                    if (i == cells.length - 1) {
                        for (int k = startX + 1; k <= endX; ++k) {
                            if (cells[0][k].getState() == 0) {
                                cellsToUpdate.put(cells[0][k], cells[i][j].getGrain());
                            }
                        }
                    }
                    if (j == 0) {
                        for (int k = startY; k < endY; ++k) {
                            if (cells[k][j].getState() == 0) {
                                cellsToUpdate.put(cells[k][j], cells[i][j].getGrain());
                            }
                        }
                    }
                    if (j == cells[i].length - 1) {
                        for (int k = startY + 1; k <= endY; ++k) {
                            if (cells[k][j].getState() == 0) {
                                cellsToUpdate.put(cells[k][j], cells[i][j].getGrain());
                            }
                        }
                    }

                    for (int k = startY; k <= endY; ++k) {
                        for (int l = startX; l <= endX; ++l) {
                            if (!(k == startY && l == endX) && !(k == endY && l == startX) && cells[k][l].getState() == 0) {
                                cellsToUpdate.put(cells[k][l], cells[i][j].getGrain());
                            }
                        }
                    }
                }
            }
        }
    }

    private void hexagonalRightClosed() {
        int startX, endX, startY, endY;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (cells[i][j].getState() != 0) {
                    startX = j == 0 ? j : j - 1;
                    endX = j == cells[i].length - 1 ? j : j + 1;
                    startY = i == 0 ? i : i - 1;
                    endY = i == cells.length - 1 ? i : i + 1;

                    for (int k = startY; k <= endY; ++k) {
                        for (int l = startX; l <= endX; ++l) {
                            if (!(k == startY && l == startX) && !(k == endY && l == endX) && cells[k][l].getState() == 0) {
                                cellsToUpdate.put(cells[k][l], cells[i][j].getGrain());
                            }
                        }
                    }
                }
            }
        }
    }

    private void hexagonalRightPeriodic() {
        int startX, endX, startY, endY;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (cells[i][j].getState() != 0) {
                    startX = j == 0 ? j : j - 1;
                    endX = j == cells[i].length - 1 ? j : j + 1;
                    startY = i == 0 ? i : i - 1;
                    endY = i == cells.length - 1 ? i : i + 1;

                    if (i == 0) {
                        for (int k = startX + 1; k <= endX; ++k) {
                            if (cells[cells.length - 1][k].getState() == 0) {
                                cellsToUpdate.put(cells[cells.length - 1][k], cells[i][j].getGrain());
                            }
                        }
                    }
                    if (i == cells.length - 1) {
                        for (int k = startX; k < endX; ++k) {
                            if (cells[0][k].getState() == 0) {
                                cellsToUpdate.put(cells[0][k], cells[i][j].getGrain());
                            }
                        }
                    }
                    if (j == 0) {
                        for (int k = startY + 1; k <= endY; ++k) {
                            if (cells[k][j].getState() == 0) {
                                cellsToUpdate.put(cells[k][j], cells[i][j].getGrain());
                            }
                        }
                    }
                    if (j == cells[i].length - 1) {
                        for (int k = startY; k < endY; ++k) {
                            if (cells[k][j].getState() == 0) {
                                cellsToUpdate.put(cells[k][j], cells[i][j].getGrain());
                            }
                        }
                    }

                    for (int k = startY; k <= endY; ++k) {
                        for (int l = startX; l <= endX; ++l) {
                            if (!(k == startY && l == startX) && !(k == endY && l == endX) && cells[k][l].getState() == 0) {
                                cellsToUpdate.put(cells[k][l], cells[i][j].getGrain());
                            }
                        }
                    }
                }
            }
        }
    }

    public void pentagonalClosed1() { // down
        int startX, endX, startY, endY;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (cells[i][j].getState() != 0) {
                    startX = j == 0 ? j : j - 1;
                    endX = j == cells[i].length - 1 ? j : j + 1;
                    startY = i;
                    endY = i == cells.length - 1 ? i : i + 1;

                    for (int k = startY; k <= endY; ++k) {
                        for (int l = startX; l <= endX; ++l) {
                            if (cells[k][l].getState() == 0) {
                                cellsToUpdate.put(cells[k][l], cells[i][j].getGrain());
                            }
                        }
                    }
                }
            }
        }
    }

    public void pentagonalClosed2() { // right
        int startX, endX, startY, endY;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (cells[i][j].getState() != 0) {
                    startX = j;
                    endX = j == cells[i].length - 1 ? j : j + 1;
                    startY = i == 0 ? i : i - 1;
                    endY = i == cells.length - 1 ? i : i + 1;

                    for (int k = startY; k <= endY; ++k) {
                        for (int l = startX; l <= endX; ++l) {
                            if (cells[k][l].getState() == 0) {
                                cellsToUpdate.put(cells[k][l], cells[i][j].getGrain());
                            }
                        }
                    }
                }
            }
        }
    }

    public void pentagonalClosed3() { // up
        int startX, endX, startY, endY;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (cells[i][j].getState() != 0) {
                    startX = j == 0 ? j : j - 1;
                    endX = j == cells[i].length - 1 ? j : j + 1;
                    startY = i == 0 ? i : i - 1;
                    endY = i;

                    for (int k = startY; k <= endY; ++k) {
                        for (int l = startX; l <= endX; ++l) {
                            if (cells[k][l].getState() == 0) {
                                cellsToUpdate.put(cells[k][l], cells[i][j].getGrain());
                            }
                        }
                    }
                }
            }
        }
    }

    public void pentagonalClosed4() { // left
        int startX, endX, startY, endY;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (cells[i][j].getState() != 0) {
                    startX = j == 0 ? j : j - 1;
                    endX = j;
                    startY = i == 0 ? i : i - 1;
                    endY = i == cells.length - 1 ? i : i + 1;

                    for (int k = startY; k <= endY; ++k) {
                        for (int l = startX; l <= endX; ++l) {
                            if (cells[k][l].getState() == 0) {
                                cellsToUpdate.put(cells[k][l], cells[i][j].getGrain());
                            }
                        }
                    }
                }
            }
        }
    }

    public void pentagonalPeriodic1() { // down
        int startX, endX, startY, endY;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (cells[i][j].getState() != 0) {
                    startX = j == 0 ? j : j - 1;
                    endX = j == cells[i].length - 1 ? j : j + 1;
                    startY = i;
                    endY = i == cells.length - 1 ? i : i + 1;

                    if (i == cells.length - 1) {
                        for (int k = startX; k <= endX; ++k) {
                            if (cells[0][k].getState() == 0) {
                                cellsToUpdate.put(cells[0][k], cells[i][j].getGrain());
                            }
                        }
                    }

                    if (j == 0) {
                        for (int k = startY; k <= endY; ++k) {
                            if (cells[k][cells.length - 1].getState() == 0) {
                                cellsToUpdate.put(cells[k][cells.length - 1], cells[i][j].getGrain());
                            }
                        }
                    }

                    if (j == cells.length - 1) {
                        for (int k = startY; k <= endY; ++k) {
                            if (cells[k][0].getState() == 0) {
                                cellsToUpdate.put(cells[k][0], cells[i][j].getGrain());
                            }
                        }
                    }

                    for (int k = startY; k <= endY; ++k) {
                        for (int l = startX; l <= endX; ++l) {
                            if (cells[k][l].getState() == 0) {
                                cellsToUpdate.put(cells[k][l], cells[i][j].getGrain());
                            }
                        }
                    }
                }
            }
        }
    }

    public void pentagonalPeriodic2() { // right
        int startX, endX, startY, endY;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (cells[i][j].getState() != 0) {
                    startX = j;
                    endX = j == cells[i].length - 1 ? j : j + 1;
                    startY = i == 0 ? i : i - 1;
                    endY = i == cells.length - 1 ? i : i + 1;

                    if (i == cells.length - 1) {
                        for (int k = startX; k <= endX; ++k) {
                            if (cells[0][k].getState() == 0) {
                                cellsToUpdate.put(cells[0][k], cells[i][j].getGrain());
                            }
                        }
                    }

                    if (i == 0) {
                        for (int k = startX; k <= endX; ++k) {
                            if (cells[cells[i].length - 1][k].getState() == 0) {
                                cellsToUpdate.put(cells[cells[i].length - 1][k], cells[i][j].getGrain());
                            }
                        }
                    }

                    if (j == cells.length - 1) {
                        for (int k = startY; k <= endY; ++k) {
                            if (cells[k][0].getState() == 0) {
                                cellsToUpdate.put(cells[k][0], cells[i][j].getGrain());
                            }
                        }
                    }

                    for (int k = startY; k <= endY; ++k) {
                        for (int l = startX; l <= endX; ++l) {
                            if (cells[k][l].getState() == 0) {
                                cellsToUpdate.put(cells[k][l], cells[i][j].getGrain());
                            }
                        }
                    }
                }
            }
        }
    }

    public void pentagonalPeriodic3() { // up
        int startX, endX, startY, endY;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (cells[i][j].getState() != 0) {
                    startX = j == 0 ? j : j - 1;
                    endX = j == cells[i].length - 1 ? j : j + 1;
                    startY = i == 0 ? i : i - 1;
                    endY = i;

                    if (i == 0) {
                        for (int k = startX; k <= endX; ++k) {
                            if (cells[cells[i].length - 1][k].getState() == 0) {
                                cellsToUpdate.put(cells[cells[i].length - 1][k], cells[i][j].getGrain());
                            }
                        }
                    }

                    if (j == 0) {
                        for (int k = startY; k <= endY; ++k) {
                            if (cells[k][cells.length - 1].getState() == 0) {
                                cellsToUpdate.put(cells[k][cells.length - 1], cells[i][j].getGrain());
                            }
                        }
                    }

                    if (j == cells.length - 1) {
                        for (int k = startY; k <= endY; ++k) {
                            if (cells[k][0].getState() == 0) {
                                cellsToUpdate.put(cells[k][0], cells[i][j].getGrain());
                            }
                        }
                    }

                    for (int k = startY; k <= endY; ++k) {
                        for (int l = startX; l <= endX; ++l) {
                            if (cells[k][l].getState() == 0) {
                                cellsToUpdate.put(cells[k][l], cells[i][j].getGrain());
                            }
                        }
                    }
                }
            }
        }
    }

    public void pentagonalPeriodic4() { // left
        int startX, endX, startY, endY;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (cells[i][j].getState() != 0) {
                    startX = j == 0 ? j : j - 1;
                    endX = j;
                    startY = i == 0 ? i : i - 1;
                    endY = i == cells.length - 1 ? i : i + 1;

                    if (i == cells.length - 1) {
                        for (int k = startX; k <= endX; ++k) {
                            if (cells[0][k].getState() == 0) {
                                cellsToUpdate.put(cells[0][k], cells[i][j].getGrain());
                            }
                        }
                    }

                    if (j == 0) {
                        for (int k = startY; k <= endY; ++k) {
                            if (cells[k][cells.length - 1].getState() == 0) {
                                cellsToUpdate.put(cells[k][cells.length - 1], cells[i][j].getGrain());
                            }
                        }
                    }

                    if (i == 0) {
                        for (int k = startX; k <= endX; ++k) {
                            if (cells[cells[i].length - 1][k].getState() == 0) {
                                cellsToUpdate.put(cells[cells[i].length - 1][k], cells[i][j].getGrain());
                            }
                        }
                    }

                    for (int k = startY; k <= endY; ++k) {
                        for (int l = startX; l <= endX; ++l) {
                            if (cells[k][l].getState() == 0) {
                                cellsToUpdate.put(cells[k][l], cells[i][j].getGrain());
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateGrid() {
        for (Map.Entry<Cell, Grain> entry : cellsToUpdate.entrySet()) {
            entry.getKey().setGrain(entry.getValue());
            entry.getKey().setState(entry.getValue().getState());
        }

        cellsToUpdate.clear();
    }

    private void draw() throws InterruptedException {
        Platform.runLater(() -> {
            for (int i = 0; i < cells.length; i++) {
                for (int j = 0; j < cells[0].length; j++) {
                    if (cells[i][j].getState() != 0) {
                        gc.setFill(cells[i][j].getGrain().getColor());
                        gc.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                    }
                }
            }
        });
    }

    private Boolean checkIfSimulationHasEnded() {
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (cells[i][j].getState() == 0)
                    return false;
            }
        }
        return true;
    }

    private boolean isGrainSpawnPossible(Map<Integer, Integer> helperMap, int a, int b, int radius) {
        for (Map.Entry<Integer, Integer> entry : helperMap.entrySet()) {
            if (Math.pow(entry.getValue() - a, 2) + Math.pow(entry.getKey() - b, 2) < Math.pow(radius, 2)) {
                return false;
            }
        }
        return true;
    }

    public void runMonteCarlo() throws InterruptedException {
        List<Cell> neighbours = new ArrayList<>();
        Platform.runLater(() -> {
            for (int iterations = 0; iterations < monteCarloIterations; ++iterations) {
                int startX, endX, startY, endY;
                for (int i = 0; i < cells.length; ++i) {
                    for (int j = 0; j < cells[i].length; ++j) {
                        startX = j == 0 ? j : j - 1;
                        endX = j == cells[i].length - 1 ? j : j + 1;
                        startY = i == 0 ? i : i - 1;
                        endY = i == cells.length - 1 ? i : i + 1;

                        for (int k = startY; k <= endY; ++k) {
                            for (int l = startX; l <= endX; ++l) {
                                if (!(k == i && l == j))
                                    neighbours.add(cells[k][l]);
                            }
                        }

                        int startGrainEnergy = calcEnergy(neighbours, cells[i][j].getState());
                        if (startGrainEnergy == 0) {
                            neighbours.clear();
                            continue;
                        }

                        Grain randomGrainInNeighbourhood;
                        int energy, newGrainState;
                        do {
                            randomGrainInNeighbourhood = neighbours.get(random.nextInt(neighbours.size() - 1)).getGrain();
                            newGrainState = randomGrainInNeighbourhood.getState();
                            energy = calcEnergy(neighbours, newGrainState);
                        } while (energy > startGrainEnergy);

                        cells[i][j].setState(newGrainState);
                        cells[i][j].getGrain().setState(newGrainState);
                        cells[i][j].getGrain().setColor(randomGrainInNeighbourhood.getColor());
                        gc.setFill(randomGrainInNeighbourhood.getColor());
                        gc.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);

                        neighbours.clear();
                    }
                }
            }
            System.out.println("Monte Carle ended with: " + monteCarloIterations + " iterations.");
        });
    }

    private int calcEnergy(List<Cell> neighbours, int value) {
        int energy = 0;
        for (Cell cellVal : neighbours) {
            if (cellVal.getState() != value)
                energy++;
        }
        return energy;
    }

    public void setMonteCarloIterations(int monteCarloIterations) {
        this.monteCarloIterations = monteCarloIterations;
    }

}
