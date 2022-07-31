package io.shabanov.jmonkeytetris.view;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Box;
import com.jme3.util.TangentBinormalGenerator;
import io.shabanov.jmonkeytetris.model.Tetrades;
import io.shabanov.jmonkeytetris.model.TetrisModel;
import io.shabanov.jmonkeytetris.model.TetrisCoordinate;
import io.shabanov.jmonkeytetris.util.Tango;
import io.shabanov.jmonkeytetris.view.presentation.TetradeColors;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * All-in-one presentation logic for a tetris gameplay field.
 *
 * TODO: more interaction with a model.
 */
public final class TetrisView {

    private final Node fieldHolder;

    private final Node fallingTetradeHolder;

    private final Node nextTetradeHolder;

    private final Spatial[][] fieldNodes;

    private final Material fallingTetradeMaterial;
    private final Material nextTetradeMaterial;

    private final Material[] cellCubeMaterials = new Material[TetradeColors.CELL_CUBE_COLORS.length];

    private final Random random;

    private final AssetManager assetManager;

    private final CellPresentationStyle cellPresentationStyle = CellPresentationStyle.PEBBLE;

    private final List<Spatial> projectedTetradeElements = new ArrayList<>();

    private final TetrisModel model;

    public enum CellPresentationStyle {
        BRICK {
            @Override
            public Spatial getSpatial(AssetManager assetManager) {
                return new Geometry("brick-cell", new Box(.5f, .5f, .5f));
            }

            @Override
            public String getMaterialName() {
                return "Materials/brick.j3m";
            }
        },

        PEBBLE {
            private Spatial prototype; //< don't attempt to load the same asset multiple times

            @Override
            public Spatial getSpatial(AssetManager assetManager) {
                if (prototype == null) {
                    prototype = assetManager.loadModel("Models/pebble.j3o");
                    TangentBinormalGenerator.generate(prototype);
                    prototype.setLocalScale(0.5f);
                }
                return prototype.clone();
            }

            @Override
            public String getMaterialName() {
                return "Materials/pebble.j3m";
            }
        };

        public abstract Spatial getSpatial(AssetManager assetManager);

        public abstract String getMaterialName();
    }

    public TetrisView(Random random, AssetManager assetManager, TetrisModel model) {
        this.random = random;
        this.assetManager = assetManager;
        this.model = model;

        for (int i = 0; i < TetradeColors.CELL_CUBE_COLORS.length; ++i) {
            this.cellCubeMaterials[i] = assetManager.loadMaterial(cellPresentationStyle.getMaterialName());
            this.cellCubeMaterials[i].setColor("Diffuse", TetradeColors.CELL_CUBE_COLORS[i]);
        }

        this.fallingTetradeMaterial = assetManager.loadMaterial(cellPresentationStyle.getMaterialName());
        this.fallingTetradeMaterial.setColor("Diffuse", Tango.ALUMINUM_1);

        this.nextTetradeMaterial = this.fallingTetradeMaterial.clone();
        this.nextTetradeMaterial.setColor("Diffuse", Tango.SCARLET_RED_1);

        this.fieldNodes = new Spatial[model.getFieldHeight()][model.getFieldWidth()];
        this.fieldHolder = new Node("fieldHolder");

        this.fallingTetradeHolder = new Node("fallingTetradeHolder");
        this.fieldHolder.attachChild(this.fallingTetradeHolder);

        this.nextTetradeHolder = new Node("nextTetradeHolder");
        this.fieldHolder.attachChild(this.nextTetradeHolder);
        this.nextTetradeHolder.setLocalTranslation(model.getFieldWidth() + 5, model.getFieldHeight(), 0);

        for (int i = 0; i < Tetrades.ELEMENTS_COUNT; ++i) {
            final Geometry projectedCell = new Geometry("projected-cell", new Box(.5f, .5f, .5f));
            final Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.getAdditionalRenderState().setWireframe(true);
            mat.setColor("Color", ColorRGBA.Orange);
            projectedCell.setMaterial(mat);
            projectedTetradeElements.add(projectedCell);
        }

        createStaticFieldElements(assetManager);

        model.addFieldChangeListener((x, y) -> {
            if (fieldNodes[y][x] != null) {
                throw new IllegalStateException("duplicate node at x=" + x + ", y=" + y);
            }
            final Spatial cellBox = getCellBox(x, y, null);
            fieldNodes[y][x] = cellBox;
            fieldHolder.attachChild(cellBox);
        });
    }

    public void setupFallingTetrade() {
        final List<TetrisCoordinate> tetradeCoordinates = model.getCurrentTetrade();
        final TetrisCoordinate tetradeCenter = model.getTetradeCenter();
        if (tetradeCoordinates == null || tetradeCenter == null) {
            return; // TODO: log error
        }

        fallingTetradeHolder.detachAllChildren();

        // TODO: instead of moving each falling tetrade cell individually, move the whole tetrade down
        for (final TetrisCoordinate c : tetradeCoordinates) {
            fallingTetradeHolder.attachChild(getCellBox(c.x + tetradeCenter.x, c.y + tetradeCenter.y, fallingTetradeMaterial));
        }

        final int projectedTetradeY = model.getProjectedTetradeY();
        for (int i = 0; i < tetradeCoordinates.size(); ++i) {
            final TetrisCoordinate coordinate = tetradeCoordinates.get(i);
            fieldHolder.attachChild(projectedTetradeElements.get(i));
            projectedTetradeElements.get(i).setLocalTranslation(
                    new Vector3f(coordinate.x + tetradeCenter.x, coordinate.y + projectedTetradeY, -1));
        }
    }

    public void updateFallingTetrade(float offset) {
        final List<TetrisCoordinate> tetradeCoordinates = model.getCurrentTetrade();
        final TetrisCoordinate tetradeCenter = model.getTetradeCenter();
        if (tetradeCoordinates == null || tetradeCenter == null) {
            return; // TODO: log error
        }

        // TODO: set local translation on a single node that unites all the tetrades
        for (int i = 0; i < tetradeCoordinates.size(); ++i) {
            final TetrisCoordinate coordinate = tetradeCoordinates.get(i);
            fallingTetradeHolder.getChild(i).setLocalTranslation(
                    new Vector3f(coordinate.x + tetradeCenter.x, coordinate.y + tetradeCenter.y + offset, -1));
        }

        // TODO: set local translation on a single node that unites all the tetrades
        final int projectedTetradeY = model.getProjectedTetradeY();
        for (int i = 0; i < tetradeCoordinates.size(); ++i) {
            final TetrisCoordinate coordinate = tetradeCoordinates.get(i);
            projectedTetradeElements.get(i).setLocalTranslation(
                    new Vector3f(coordinate.x + tetradeCenter.x, coordinate.y + projectedTetradeY, -1));
        }
    }

    public void updateNextTetrade() {
        final List<TetrisCoordinate> coordinates = model.getNextTetrade();
        if (coordinates == null) {
            return; // TODO: warning
        }

        if (nextTetradeHolder.getQuantity() == 0) {
            for (int i = 0; i < Tetrades.ELEMENTS_COUNT; ++i) {
                final Spatial cell = getCellBox(0, 0, this.nextTetradeMaterial);
                nextTetradeHolder.attachChild(cell);
            }
        }

        for (int i = 0; i < coordinates.size(); ++i) {
            final TetrisCoordinate c = coordinates.get(i);
            nextTetradeHolder.getChild(i).setLocalTranslation(c.x, c.y, -1);
        }
    }

    public void removeLines(List<Integer> linesToClear) {
        for (int i = linesToClear.size() - 1; i >= 0; --i) {
            final int line = linesToClear.get(i);
            // detach all children
            for (final Spatial spatialToRemove : fieldNodes[line]) {
                assert spatialToRemove != null;
                fieldHolder.detachChild(spatialToRemove);
            }
            // copy lines
            for (int yScan = line + 1; yScan < model.getFieldHeight(); ++yScan) {
                for (final Spatial spatialToMove : fieldNodes[yScan]) {
                    if (spatialToMove != null) {
                        spatialToMove.move(0, -1f, 0);
                    }
                }
                System.arraycopy(fieldNodes[yScan], 0, fieldNodes[yScan - 1], 0, model.getFieldWidth());
            }
        }

        // make projected tetrade disappear
        for (Spatial projectedTetradeElement : projectedTetradeElements) {
            fieldHolder.detachChild(projectedTetradeElement);
        }
    }

    public void clear() {
        // detach all children
        for (int y = 0; y < fieldNodes.length; ++y) {
            final Spatial[] line = fieldNodes[y];
            for (int x = 0; x < line.length; ++x) {
                if (fieldNodes[y][x] != null) {
                    fieldHolder.detachChild(fieldNodes[y][x]);
                    fieldNodes[y][x] = null;
                }
            }
        }

        model.updateFieldListeners();
    }

    public Node getHolderNode() {
        return fieldHolder;
    }

    public void updateStaticAnimation(float tpf) {
        // update next tetrade rotation
        this.nextTetradeHolder.rotate(0, tpf * 0.7f, 0);
    }

    //
    // Private
    //

    private void createStaticFieldElements(AssetManager assetManager) {
        final Node gridHolder = new Node();
        fieldHolder.attachChild(gridHolder);

        { // add grid pad
            final int fieldHorizontalDimension = model.getFieldWidth() + 1;
            final Geometry wireGrid = new Geometry("wireframe-grid-bottom", new Grid(4, fieldHorizontalDimension, 1f));
            final Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.getAdditionalRenderState().setWireframe(true);
            mat.setColor("Color", ColorRGBA.Red);
            wireGrid.setMaterial(mat);
            wireGrid.center().move(new Vector3f(-fieldHorizontalDimension / 2f, 0, 0));
            gridHolder.attachChild(wireGrid);
        }
        { // add grid left
            final Geometry wireGrid = new Geometry("wireframe-grid-left", new Grid(4, model.getFieldHeight(), 1f));
            final Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.getAdditionalRenderState().setWireframe(true);
            mat.setColor("Color", ColorRGBA.Green);
            wireGrid.setMaterial(mat);
            wireGrid.center().move(new Vector3f(0, 0, 0));
            wireGrid.rotate(0, 0,  FastMath.HALF_PI);
            gridHolder.attachChild(wireGrid);
        }
        { // add grid right
            final Geometry wireGrid = new Geometry("wireframe-grid-left", new Grid(4, model.getFieldHeight(), 1f));
            final Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.getAdditionalRenderState().setWireframe(true);
            mat.setColor("Color", ColorRGBA.Blue);
            wireGrid.setMaterial(mat);
            wireGrid.center().move(new Vector3f(model.getFieldWidth(), 0, 0));
            wireGrid.rotate(0, 0, FastMath.HALF_PI);
            gridHolder.attachChild(wireGrid);
        }
        gridHolder.setLocalTranslation(model.getFieldWidth(), -.5f, -1);
    }

    private Spatial getCellBox(int x, int y, @Nullable Material optionalMaterial) {
        final Spatial spatialCell = cellPresentationStyle.getSpatial(assetManager);

        spatialCell.setLocalTranslation(new Vector3f(x, y, -1));

        final Material material = optionalMaterial != null ? optionalMaterial : cellCubeMaterials[random.nextInt(cellCubeMaterials.length)];
        spatialCell.setMaterial(material);

        return spatialCell;
    }
}
