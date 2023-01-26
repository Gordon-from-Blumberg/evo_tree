package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.DateTimeFormatter;

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

    private final DateTimeFormatter dateTimeFormatter = new DateTimeFormatter(false);

    private final FileList fileList = new FileList();
    private FileHandle saveDir;
    private Table fileTable;
    private TextButton saveLoadButton;
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
        add(fileTable).expand().fillX().align(Align.top).minWidth(0).pad(10f);

        row();
        Table buttons = new Table(skin);
        add(buttons).align(Align.bottom);
        saveLoadButton = new TextButton("", skin);
        saveLoadButton.addListener(new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!saveLoadButton.isDisabled()) {
                    saveOrLoad();
                    close();
                }
            }
        });
        buttons.add(saveLoadButton).expandX().align(Align.left).pad(10f);
        TextButton cancelButton = new TextButton("Cancel", skin);
        cancelButton.addListener(new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
            }
        });
        buttons.add(cancelButton).align(Align.right).pad(10f);

        extensionFilter = createFileFilter(extension);

        setOnOpenHandler(this::onOpen);
    }

    public void setLoad() {
        type = Type.LOAD;
    }

    private void saveOrLoad() {

    }

    private void onOpen() {
        saveLoadButton.setText(type == Type.SAVE ? "Save" : "Load");

        File[] files = saveDir.file().listFiles(extensionFilter);
        if (files != null) {
            Arrays.sort(files, fileComparator);

            fileList.start();
            for (File file : files) {
                fileList.add(file);
            }
            fileList.end();

        } else {
            throw new IllegalStateException(saveDir.path() + " is not a directory");
        }
    }

    private class FileList {
        private final Array<FileRow> list = new Array<>();
        private FileRow selected;
        private int index;

        void start() {
            index = 0;
        }

        void add(File file) {
            FileRow row;
            if (index < list.size) {
                row = list.get(index);
            } else {
                list.add(row = createRow());
            }

            row.addToTable();
            row.nameLabel.setText(file.getName());
            row.lastModifiedLabel.setText(dateTimeFormatter.format(file.lastModified()));
            row.file = file;

            ++index;
        }

        void end() {
            for (int i = index; i < list.size; ++i) {
                FileRow row = list.get(i);
                row.nameCell.clearActor();
                row.lastModifiedCell.clearActor();
                row.file = null;
            }
        }

        FileRow createRow() {
            Skin skin = getSkin();
            FileRow fileRow = new FileRow();
            fileTable.row();

            fileRow.nameLabel = new Label("", skin);
            fileRow.nameLabel.setUserObject(fileRow);
            fileRow.nameCell = fileTable.add(fileRow.nameLabel).padRight(5f);

            fileRow.lastModifiedLabel = new Label("", skin);
            fileRow.lastModifiedLabel.setUserObject(fileRow);
            fileRow.lastModifiedLabel.setColor(Color.LIGHT_GRAY);
            fileRow.lastModifiedCell = fileTable.add(fileRow.lastModifiedLabel);

            return fileRow;
        }
    }

    private static class FileRow {
        private File file;
        private Label nameLabel;
        private Cell<Label> nameCell;
        private Label lastModifiedLabel;
        private Cell<Label> lastModifiedCell;

        void addToTable() {
            nameCell.setActor(nameLabel);
            lastModifiedCell.setActor(lastModifiedLabel);
        }
    }

    private FilenameFilter createFileFilter(String extension) {
        String dotExtension = '.' + extension;
        return (dir, name) -> name.endsWith(dotExtension);
    }
}
