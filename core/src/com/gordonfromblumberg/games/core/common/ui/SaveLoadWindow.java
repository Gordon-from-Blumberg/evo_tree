package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

public class SaveLoadWindow extends UIWindow {
    private enum Type {
        SAVE, LOAD
    }

    private static final Logger log = LogManager.create(SaveLoadWindow.class);
    private static final Comparator<File> fileComparator = (o1, o2) -> (int) (o2.lastModified() - o1.lastModified());

    private final Pool<Label> labelPool = new Pool<Label>() {
        @Override
        protected Label newObject() {
            return new Label("", getSkin());
        }
    };
    private FileHandle saveDir;
    private Table fileTable;
    private String fileExtension;
    private Type type = Type.SAVE;
    private FilenameFilter extensionFilter;

    public SaveLoadWindow(String title, Skin skin, String path, String extension) {
        super(title, skin);

        ConfigManager config = AbstractFactory.getInstance().configManager();
        saveDir = Main.WORK_DIR.child(path);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        fileTable = new Table(skin);
        add(fileTable);

        extensionFilter = createFileFilter(extension);

        setOnOpenHandler(this::fillTable);
    }

    public void setLoad() {
        type = Type.LOAD;
    }

    @SuppressWarnings("unchecked")
    private void fillTable() {
        File[] files = saveDir.file().listFiles(extensionFilter);
        if (files != null) {
            Arrays.sort(files, fileComparator);
            Array<Cell> cells = fileTable.getCells();
            int i, j, size;
            // fill table by actual file info
            for (i = 0, j = 0, size = files.length; i < size; ++i, j += 2) {
                final File file = files[i];
                if (cells.size >= (i + 1) * 2) {
                    Cell<Label> nameCell = (Cell<Label>) cells.get(j);
                    if (nameCell.getActor() == null) {
                        nameCell.setActor(labelPool.obtain());
                    }
                    nameCell.getActor().setText(file.getName());
                    Cell<Label> lastModifiedCell = (Cell<Label>) cells.get(j + 1);
                    if (lastModifiedCell.getActor() == null) {
                        lastModifiedCell.setActor(labelPool.obtain());
                    }
                    lastModifiedCell.getActor().setText(String.valueOf(file.lastModified()));
                } else {
                    Label nameLabel = labelPool.obtain();
                    nameLabel.setText(file.getName());
                    fileTable.add(nameLabel);
                    Label lastModifiedLabel = labelPool.obtain();
                    lastModifiedLabel.setText(String.valueOf(file.lastModified()));
                    fileTable.add(lastModifiedLabel);
                    fileTable.row();
                }
            }

            // if file count is less than table rows, hide them
            while (cells.size > j) {
                Cell<Label> cell = cells.get(j++);
                if (cell.getActor() != null) {
                    labelPool.free(cell.getActor());
                    cell.clearActor();
                }
            }

        } else {
            throw new IllegalStateException(saveDir.path() + " is not a directory");
        }
    }

    private FilenameFilter createFileFilter(String extension) {
        String dotExtension = '.' + extension;
        return (dir, name) -> name.endsWith(dotExtension);
    }
}
