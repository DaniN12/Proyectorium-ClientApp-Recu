package clientapp.factories;

import clientapp.model.TicketEntity;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.DateCell;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class TicketDatePickerTableCell extends TableCell<TicketEntity, Date> {

    private DatePicker datePicker;
    private TableView<TicketEntity> tableMovies;

    public TicketDatePickerTableCell(TableView<TicketEntity> tableMovies) {
        this.tableMovies = tableMovies;
    }

    public TicketDatePickerTableCell() {

    }

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createDatePicker();
            setText(null);
            setGraphic(datePicker);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(formatDate(getItem()));
        setGraphic(null);
    }

    @Override
    public void updateItem(Date item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (datePicker != null) {
                    datePicker.setValue(convertToLocalDate(item));
                }
                setText(null);
                setGraphic(datePicker);
            } else {
                setText(formatDate(item));
                setGraphic(null);
            }
        }
    }

    private void createDatePicker() {
        datePicker = new DatePicker(convertToLocalDate(getItem()));
        datePicker.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);

        // Disable future dates
        final Callback<DatePicker, DateCell> dayCellFactory = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        };

        datePicker.setDayCellFactory(dayCellFactory);

        // Commit the value when focus is lost or a date is selected
        datePicker.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
                commitEdit(convertToDate(datePicker.getValue()));
            }
        });

        datePicker.setOnAction(e -> commitEdit(convertToDate(datePicker.getValue())));
    }

    private String formatDate(Date date) {
        return date == null ? "" : java.text.DateFormat.getDateInstance().format(date);
    }

    private LocalDate convertToLocalDate(Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Date convertToDate(LocalDate localDate) {
        return localDate == null ? null : Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
