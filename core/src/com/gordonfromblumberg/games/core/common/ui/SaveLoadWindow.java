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

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;

public class SaveLoadWindow extends UIWindow {
    private enum Type {
        SAVE, LOAD
    }

    private static final Color DEFAULT_FONT_COLOR = Color.WHITE;
    private static final Color MUTED_FONT_COLOR = Color.LIGHT_GRAY;
    private static final Color SELECTED_FONT_COLOR = Color.YELLOW;
    private static final Color SELECTED_MUTED_FONT_COLOR = new Color(SELECTED_FONT_COLOR).mul(MUTED_FONT_COLOR);

    private static final Logger log = LogManager.create(SaveLoadWindow.class);
    private static final Comparator<File> fileComparator = (o1, o2) -> {
        long diff = o2.lastModified() - o1.lastModified();
        return diff > 0 ? 1 : diff < 0 ? -1 : 0;
    };

    private final DateTimeFormatter dateTimeFormatter = new DateTimeFormatter(false);
    private final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1000);

    private final FileList fileList = new FileList();
    private FileHandle saveDir;
    private Table fileTable;
    private TextButton saveLoadButton;
    private String fileExtension;
    private final Type type;
    private FilenameFilter extensionFilter;

    private Consumer<ByteBuffer> handler;

    public SaveLoadWindow(Skin skin, String path, String extension, boolean load) {
        super(load ? "Load" : "Save", skin);

        type = load ? Type.LOAD : Type.SAVE;
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
        saveLoadButton = new TextButton(load ? "Load" : "Save", skin);
        saveLoadButton.setDisabled(true);
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

        fileTable.addListener(new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (event.getTarget() instanceof Label && event.getTarget().getUserObject() instanceof FileRow) {
                    int clickCount = getTapCount();
                    FileRow fileRow = (FileRow) event.getTarget().getUserObject();
                    if (clickCount == 1) {
                        fileList.select(fileRow);
                    } else if (clickCount == 2 && fileRow == fileList.selected) {
                        saveOrLoad();
                        close();
                        setTapCount(0);
                    }
                }
            }
        });
        setOnOpenHandler(this::onOpen);
        setOnCloseHandler(this::onClose);
    }

    public void open(Consumer<ByteBuffer> handler) {
        this.handler = handler;
        open();
    }

    private void saveOrLoad() {
        if (fileList.selected != null) {
            byteBuffer.clear();
            if (type == Type.LOAD) {
                try (FileInputStream is = new FileInputStream(fileList.selected.file)) {
                    is.getChannel().read(byteBuffer);
                    byteBuffer.flip();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            handler.accept(byteBuffer);
            if (type == Type.SAVE) {
                try (FileOutputStream os = new FileOutputStream(fileList.selected.file)) {
                    byteBuffer.flip();
                    os.getChannel().write(byteBuffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void onOpen() {
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

    private void onClose() {
        fileList.unselect();
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

        void select(FileRow fileRow) {
            if (fileRow != selected) {
                if (selected != null) {
                    unselect();
                }
                fileRow.nameLabel.setColor(SELECTED_FONT_COLOR);
                fileRow.lastModifiedLabel.setColor(SELECTED_MUTED_FONT_COLOR);
                selected = fileRow;
                saveLoadButton.setDisabled(false);
            }
        }

        void unselect() {
            if (selected != null) {
                selected.nameLabel.setColor(DEFAULT_FONT_COLOR);
                selected.lastModifiedLabel.setColor(MUTED_FONT_COLOR);
                selected = null;
                saveLoadButton.setDisabled(true);
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
