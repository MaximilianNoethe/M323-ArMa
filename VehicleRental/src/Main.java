import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class Main {
    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color TEXT_DARK = new Color(31, 41, 55);
    private static final Color TEXT_MUTED = new Color(82, 95, 116);
    private static final Color BORDER = new Color(222, 226, 233);
    private static final Color GREEN = new Color(22, 120, 75);
    private static final Color ORANGE = new Color(184, 88, 37);
    private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 28);
    private static final Font SECTION_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 20);
    private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 13);

    private RentalState rentalState;
    private JComboBox<Vehicle> vehicleComboBox;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField birthDateField;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextArea documentEmailArea;
    private JCheckBox documentsSubmittedCheckBox;
    private JPanel vehicleCardsPanel;
    private JLabel totalRevenueLabel;

    private Person documentEmailPerson;
    private Vehicle documentEmailVehicle;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().start());
    }

    private void start() {
        rentalState = createStartState();
        useSystemLookAndFeel();

        JFrame frame = new JFrame("Vehicle Rental");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1180, 760));
        frame.setContentPane(createMainPanel());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        refreshVehicleCards();
    }

    private RentalState createStartState() {
        Car nissan = new Car(1500, 35.00, "Benzin", "Nissan", 4, 4);
        Car mercedes = new Car(2100, 80.50, "Elektro", "Mercedes-Benz", 4, 4);
        AirVehicle helicopter = new AirVehicle(3000, 200.00, "Turbine", "Bell", 6, 12.5);
        WaterVehicle boat = new WaterVehicle(5000, 150.00, "Diesel", "Yamaha", 10, false);

        Person blockedCustomer = new Person(LocalDate.of(2007, 4, 24), "Aryan", "aryan@example.com");
        Person existingCustomer = new Person(LocalDate.of(2000, 1, 16), "Max", "max@example.com");

        Contract activeRental = new Contract(
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(4),
            "Premium-Versicherung",
            existingCustomer,
            mercedes,
            true,
            true
        );

        return new RentalState(
            List.of(blockedCustomer, existingCustomer),
            List.of(blockedCustomer),
            List.of(nissan, mercedes, helicopter, boat),
            List.of(activeRental)
        );
    }

    private void useSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Falls das System-Design nicht geladen werden kann, nutzt Swing sein Standard-Design.
        }
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(24, 0));
        mainPanel.setBackground(BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        mainPanel.add(createOverviewPanel(), BorderLayout.CENTER);
        mainPanel.add(createRentalPanel(), BorderLayout.EAST);
        return mainPanel;
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        JLabel title = new JLabel("Vehicle-Verfügbarkeit");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_DARK);
        panel.add(title, BorderLayout.NORTH);

        vehicleCardsPanel = new JPanel();
        vehicleCardsPanel.setOpaque(false);
        vehicleCardsPanel.setLayout(new BoxLayout(vehicleCardsPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(vehicleCardsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BACKGROUND);
        panel.add(scrollPane, BorderLayout.CENTER);

        totalRevenueLabel = new JLabel();
        totalRevenueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        totalRevenueLabel.setForeground(TEXT_MUTED);
        panel.add(totalRevenueLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createRentalPanel() {
        JPanel panel = createWhitePanel();
        panel.setPreferredSize(new Dimension(400, 0));
        panel.setLayout(new BorderLayout(0, 16));

        JLabel title = new JLabel("Vehicle mieten");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_DARK);
        panel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        vehicleComboBox = new JComboBox<>(rentalState.vehicles().toArray(Vehicle[]::new));
        vehicleComboBox.setRenderer(new VehicleComboBoxRenderer());
        vehicleComboBox.addActionListener(event -> resetDocumentSimulation());
        formPanel.add(createFormField("Vehicle auswählen", vehicleComboBox));

        nameField = new JTextField();
        formPanel.add(createFormField("Name", nameField));

        emailField = new JTextField();
        formPanel.add(createFormField("E-Mail-Adresse", emailField));

        birthDateField = new JTextField(LocalDate.now().minusYears(18).toString());
        formPanel.add(createFormField("Geburtsdatum (JJJJ-MM-TT)", birthDateField));

        startDateField = new JTextField(LocalDate.now().toString());
        formPanel.add(createFormField("Startdatum (JJJJ-MM-TT)", startDateField));

        endDateField = new JTextField(LocalDate.now().plusDays(2).toString());
        formPanel.add(createFormField("Enddatum (JJJJ-MM-TT)", endDateField));

        JButton emailButton = createSecondaryButton("Unterlagen-E-Mail simulieren");
        emailButton.addActionListener(event -> simulateDocumentEmail());
        formPanel.add(emailButton);
        formPanel.add(Box.createVerticalStrut(12));

        JLabel emailLabel = createLabel("Simulierte E-Mail");
        formPanel.add(emailLabel);
        formPanel.add(Box.createVerticalStrut(6));

        documentEmailArea = new JTextArea("Noch keine E-Mail simuliert.");
        documentEmailArea.setEditable(false);
        documentEmailArea.setLineWrap(true);
        documentEmailArea.setWrapStyleWord(true);
        documentEmailArea.setRows(8);
        documentEmailArea.setBackground(new Color(249, 250, 252));
        documentEmailArea.setForeground(TEXT_MUTED);
        documentEmailArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane emailScrollPane = new JScrollPane(documentEmailArea);
        emailScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailScrollPane.setPreferredSize(new Dimension(340, 150));
        emailScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        formPanel.add(emailScrollPane);
        formPanel.add(Box.createVerticalStrut(12));

        documentsSubmittedCheckBox = new JCheckBox("Führerschein und ID wurden simuliert mitgeschickt");
        documentsSubmittedCheckBox.setOpaque(false);
        documentsSubmittedCheckBox.setForeground(TEXT_DARK);
        documentsSubmittedCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(documentsSubmittedCheckBox);
        formPanel.add(Box.createVerticalStrut(14));

        JButton rentButton = createPrimaryButton("Vehicle mieten");
        rentButton.addActionListener(event -> rentSelectedVehicle());
        formPanel.add(rentButton);

        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFormField(String labelText, Component inputField) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(new EmptyBorder(0, 0, 12, 0));

        panel.add(createLabel(labelText), BorderLayout.NORTH);
        styleInput(inputField);
        panel.add(inputField, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_MUTED);
        return label;
    }

    private void styleInput(Component inputField) {
        inputField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        inputField.setPreferredSize(new Dimension(340, 34));
        inputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        button.setForeground(TEXT_DARK);
        button.setBackground(new Color(224, 235, 252));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return button;
    }

    private JPanel createWhitePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(20, 20, 20, 20)
        ));
        return panel;
    }

    private void refreshVehicleCards() {
        vehicleCardsPanel.removeAll();

        for (Vehicle vehicle : rentalState.vehicles()) {
            vehicleCardsPanel.add(createVehicleCard(vehicle));
            vehicleCardsPanel.add(Box.createVerticalStrut(12));
        }

        double totalRevenue = RentalLogic.calculateTotalContractValueRecursive(rentalState.contracts());
        totalRevenueLabel.setText("Rekursive Umsatzberechnung aller Mieten: CHF " + formatMoney(totalRevenue));

        vehicleCardsPanel.revalidate();
        vehicleCardsPanel.repaint();
    }

    private JPanel createVehicleCard(Vehicle vehicle) {
        JPanel card = createWhitePanel();
        card.setLayout(new BorderLayout(18, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
        card.setPreferredSize(new Dimension(100, 118));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(RentalLogic.getVehicleDescription(vehicle));
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        title.setForeground(TEXT_DARK);

        JLabel details = new JLabel(RentalLogic.getVehicleDetails(vehicle));
        details.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        details.setForeground(TEXT_MUTED);

        JLabel price = new JLabel("Preis: CHF " + formatMoney(vehicle.pricePerHour()) + " pro Stunde");
        price.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        price.setForeground(TEXT_MUTED);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(details);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(price);

        JLabel status = new JLabel(RentalLogic.getAvailabilityText(rentalState, vehicle, LocalDate.now()));
        status.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        status.setForeground(RentalLogic.isAvailableOn(rentalState, vehicle, LocalDate.now()) ? GREEN : ORANGE);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        statusPanel.setOpaque(false);
        statusPanel.add(status);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(statusPanel, BorderLayout.EAST);
        return card;
    }

    private void simulateDocumentEmail() {
        Person person = readPersonFromForm();
        if (person == null) {
            return;
        }

        Vehicle vehicle = getSelectedVehicle();
        documentEmailPerson = person;
        documentEmailVehicle = vehicle;
        documentEmailArea.setForeground(TEXT_DARK);
        documentEmailArea.setText(RentalLogic.createDocumentRequestEmail(person, vehicle));
    }

    private void rentSelectedVehicle() {
        Person person = readPersonFromForm();
        if (person == null) {
            return;
        }

        Vehicle vehicle = getSelectedVehicle();
        LocalDate startDate = readDateFromField(startDateField, "Startdatum");
        LocalDate endDate = readDateFromField(endDateField, "Enddatum");

        if (startDate == null || endDate == null) {
            return;
        }

        boolean emailWasSimulatedForThisRental = person.equals(documentEmailPerson)
            && vehicle.equals(documentEmailVehicle);

        Contract contract = new Contract(
            startDate,
            endDate,
            "Unterlagen wurden simuliert",
            person,
            vehicle,
            emailWasSimulatedForThisRental,
            documentsSubmittedCheckBox.isSelected()
        );

        Result<RentalState> result = RentalLogic.createContract(rentalState, contract);

        if (result instanceof Result.Success<RentalState> success) {
            rentalState = success.value();
            refreshVehicleCards();
            resetDocumentSimulation();
            showInfo("Die Miete wurde gespeichert.");
        } else if (result instanceof Result.Failure<RentalState> failure) {
            showWarning(failure.reason());
        }
    }

    private Person readPersonFromForm() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isBlank()) {
            showWarning("Bitte gib zuerst deinen Namen ein.");
            return null;
        }

        if (!RentalLogic.isValidEmail(email)) {
            showWarning("Bitte gib zuerst eine gültige E-Mail-Adresse ein.");
            return null;
        }

        LocalDate birthDate = readDateFromField(birthDateField, "Geburtsdatum");

        if (birthDate == null) {
            return null;
        }

        return new Person(birthDate, name, email);
    }

    private LocalDate readDateFromField(JTextField field, String fieldName) {
        try {
            return LocalDate.parse(field.getText().trim());
        } catch (DateTimeParseException exception) {
            showWarning(fieldName + " muss im Format JJJJ-MM-TT eingegeben werden, zum Beispiel 2006-04-21.");
            return null;
        }
    }

    private Vehicle getSelectedVehicle() {
        return (Vehicle) vehicleComboBox.getSelectedItem();
    }

    private void resetDocumentSimulation() {
        documentEmailPerson = null;
        documentEmailVehicle = null;
        documentsSubmittedCheckBox.setSelected(false);
        documentEmailArea.setForeground(TEXT_MUTED);
        documentEmailArea.setText("Noch keine E-Mail simuliert.");
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(null, message, "Erfolg", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(null, message, "Hinweis", JOptionPane.WARNING_MESSAGE);
    }

    private String formatMoney(double amount) {
        return String.format("%.2f", amount);
    }

    private static class VehicleComboBoxRenderer extends JLabel implements ListCellRenderer<Vehicle> {
        @Override
        public Component getListCellRendererComponent(
            JList<? extends Vehicle> list,
            Vehicle vehicle,
            int index,
            boolean isSelected,
            boolean cellHasFocus
        ) {
            setOpaque(true);
            setBorder(new EmptyBorder(6, 8, 6, 8));
            setText(vehicle == null ? "" : RentalLogic.getVehicleDescription(vehicle));
            setBackground(isSelected ? new Color(220, 234, 255) : Color.WHITE);
            setForeground(TEXT_DARK);
            return this;
        }
    }
}
