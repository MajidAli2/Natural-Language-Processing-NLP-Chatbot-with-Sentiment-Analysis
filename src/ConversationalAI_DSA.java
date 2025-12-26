import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.util.*;
import okhttp3.*;
import org.json.*;

/**
 * ConversationalAI_DSA.java
 * --------------------------
 * Conversational AI for Customer Service using DSA (Queue, Stack, HashMap,
 * Trie, KMP)
 * and OpenRouter API.
 *
 * Features:
 * - Modern GUI with multiple model support
 * - Enhanced customer service focus
 * - Professional business appearance
 * - Advanced DSA implementations
 */

public class ConversationalAI_DSA extends JFrame {

    // GUI Components
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton, undoButton, clearButton;
    private JComboBox<String> modelComboBox;
    private JLabel statusLabel, logoLabel;
    private JPanel topBar;

    // DSA structures
    private Queue<String> messageQueue = new LinkedList<>();
    private Stack<String> undoStack = new Stack<>();
    private HashMap<String, String> intentMap = new HashMap<>();
    private TrieNode root = new TrieNode();

    // API Configuration
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static String API_KEY = "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";
    private String CurrentModel = "qwen/qwen3-235b-a22b:free";

    // Available models from OpenRouter (Free and Paid)
    private String[] openRouterModels = {
            "z-ai/glm-4.5-air:free",
            "meituan/longcat-flash-chat:free",
            "nvidia/nemotron-nano-12b-v2-vl:free",

    };

    // Professional Business Colors
    private final Color PRIMARY_COLOR = new Color(0, 100, 180); // Corporate Blue
    private final Color SECONDARY_COLOR = new Color(245, 247, 250);
    private final Color ACCENT_COLOR = new Color(0, 150, 136); // Teal
    private final Color WARNING_COLOR = new Color(220, 60, 50);
    private final Color TEXT_COLOR = new Color(50, 50, 50);
    private final Color BORDER_COLOR = new Color(210, 215, 220);
    private final Color USER_MSG_COLOR = new Color(70, 130, 180); // Steel Blue for user messages

    public ConversationalAI_DSA() {
        initializeGUI();
        setupIntents();
        buildTrie();
    }

    private void initializeGUI() {
        setTitle("Customer Service AI Assistant");
        setSize(1100, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Create top bar with professional branding
        createTopBar();
        add(topBar, BorderLayout.NORTH);

        // Create main content area
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(SECONDARY_COLOR);

        // Create chat container with sidebar
        JPanel chatContainer = createChatContainer();
        mainPanel.add(chatContainer, BorderLayout.CENTER);

        // Create input panel
        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Display welcome message
        displayWelcomeMessage();
    }

    private void createTopBar() {
        topBar = new JPanel(new BorderLayout(20, 10));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
                new EmptyBorder(15, 25, 15, 25)));

        // Left section: Logo and title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setBackground(Color.WHITE);

        // Logo
        try {
            ImageIcon originalIcon = new ImageIcon("logo.png");
            Image scaledImage = originalIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            logoLabel = new JLabel(new ImageIcon(scaledImage));
        } catch (Exception e) {
            logoLabel = new JLabel("🏢");
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            logoLabel.setForeground(PRIMARY_COLOR);
        }

        // Title with branding
        JLabel titleLabel = new JLabel("<html><div style='text-align: center;'>" +
                "<b style='font-size: 18px; color: #0064b4;'>Natural Language Processing (NLP) Chatbot with Sentiment Analysis</b><br>"
                +
                "<span style='font-size: 11px; color: #666;'>PROFESSIONAL SUPPORT • SINCE 2025</span></div></html>");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        leftPanel.add(logoLabel);
        leftPanel.add(titleLabel);

        // Right section: Model selection
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(Color.WHITE);

        JLabel modelLabel = new JLabel("AI Model:");
        modelLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        modelLabel.setForeground(new Color(80, 80, 80));

        modelComboBox = new JComboBox<>(openRouterModels);
        modelComboBox.setSelectedItem(CurrentModel);
        modelComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        modelComboBox.setBackground(Color.WHITE);
        modelComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(6, 10, 6, 10)));
        modelComboBox.addActionListener(e -> {
            CurrentModel = (String) modelComboBox.getSelectedItem();
            statusLabel.setText("Model switched to: " + CurrentModel);
            displayMessage("System: Switched to " + CurrentModel,
                    new Color(100, 100, 100), true);
        });

        rightPanel.add(modelLabel);
        rightPanel.add(modelComboBox);

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);
    }

    private JPanel createChatContainer() {
        JPanel chatContainer = new JPanel(new BorderLayout(15, 0));
        chatContainer.setBackground(SECONDARY_COLOR);

        // Left sidebar with service information
        JPanel sidebarPanel = createSidebarPanel();
        chatContainer.add(sidebarPanel, BorderLayout.WEST);

        // ConversationalAI_DSA.Main chat area
        createChatArea();
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Color.WHITE);

        chatContainer.add(scrollPane, BorderLayout.CENTER);

        return chatContainer;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(250, 252, 255));
        sidebarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(25, 20, 25, 20)));
        sidebarPanel.setPreferredSize(new Dimension(200, 0));

        // Company information header
        JLabel companyHeader = new JLabel("CUSTOMER SUPPORT");
        companyHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        companyHeader.setForeground(PRIMARY_COLOR);
        companyHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Services list
        String[] services = {
                "• Account Management",
                "• Order Tracking",
                "• Payment Issues",
                "• Refund Processing",
                "• Technical Support",
                "• Product Information",
                "• Service Details",
                "• Billing Inquiries",
                "• Return Policies",
                "• Warranty Claims"
        };

        JPanel servicesPanel = new JPanel();
        servicesPanel.setLayout(new BoxLayout(servicesPanel, BoxLayout.Y_AXIS));
        servicesPanel.setBackground(new Color(250, 252, 255));
        servicesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (String service : services) {
            JLabel serviceLabel = new JLabel(service);
            serviceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            serviceLabel.setForeground(TEXT_COLOR);
            serviceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            serviceLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
            servicesPanel.add(serviceLabel);
        }

        // Add spacing and components
        sidebarPanel.add(companyHeader);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebarPanel.add(servicesPanel);
        sidebarPanel.add(Box.createVerticalGlue());

        return sidebarPanel;
    }

    private void createChatArea() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(Color.WHITE);
        chatArea.setForeground(TEXT_COLOR);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBorder(new EmptyBorder(25, 25, 25, 25));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBackground(SECONDARY_COLOR);
        inputPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        // Status label
        statusLabel = new JLabel("Ready to assist with customer service inquiries...");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(100, 100, 100));
        statusLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusPanel.setBackground(SECONDARY_COLOR);
        statusPanel.add(statusLabel);

        // Input field with button panel
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBackground(Color.WHITE);
        inputField.setForeground(TEXT_COLOR);
        inputField.setCaretColor(PRIMARY_COLOR);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(12, 15, 12, 15)));
        inputField.putClientProperty("JTextField.placeholderText",
                "Type your customer service question here...");

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 8, 0));
        buttonPanel.setBackground(SECONDARY_COLOR);

        sendButton = createStyledButton("Send Message", PRIMARY_COLOR);
        undoButton = createStyledButton("Undo", new Color(120, 120, 120));
        clearButton = createStyledButton("Clear Chat", WARNING_COLOR);

        buttonPanel.add(sendButton);
        buttonPanel.add(undoButton);
        buttonPanel.add(clearButton);

        JPanel inputContainer = new JPanel(new BorderLayout(12, 0));
        inputContainer.setBackground(SECONDARY_COLOR);
        inputContainer.add(inputField, BorderLayout.CENTER);
        inputContainer.add(buttonPanel, BorderLayout.EAST);

        inputPanel.add(statusPanel, BorderLayout.NORTH);
        inputPanel.add(inputContainer, BorderLayout.CENTER);

        // Add event listeners
        sendButton.addActionListener(e -> handleSend());
        undoButton.addActionListener(e -> handleUndo());
        clearButton.addActionListener(e -> handleClear());
        inputField.addActionListener(e -> handleSend());

        return inputPanel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private void displayWelcomeMessage() {
        String welcomeMsg = "Customer Service AI Assistant\n\n" +
                "Welcome to our professional customer service portal! I'm here to assist you with company-related inquiries.\n\n"
                +
                "Available Support Areas:\n" +
                "• Account management and login issues\n" +
                "• Order status and tracking information\n" +
                "• Payment processing and billing inquiries\n" +
                "• Refund and return policy information\n" +
                "• Product specifications and details\n" +
                "• Technical support and troubleshooting\n" +
                "• Service availability and pricing\n" +
                "• Shipping and delivery questions\n" +
                "• Warranty and claim processing\n\n" +
                "Current AI Model: " + CurrentModel + "\n\n" +
                "How may I assist you with our services today?";

        displayMessage(welcomeMsg, PRIMARY_COLOR, true);
    }

    // Handle send button
    private void handleSend() {
        String userInput = inputField.getText().trim();
        if (userInput.isEmpty())
            return;

        displayMessage("You: " + userInput, USER_MSG_COLOR, false);
        inputField.setText("");
        messageQueue.add(userInput);
        undoStack.push(userInput);

        // Update status
        statusLabel.setText("Processing your customer service request using " + CurrentModel + "...");

        if (isIrrelevant(userInput)) {
            String rejectionMsg = "Customer Service Focus\n\n" +
                    "I specialize exclusively in company-related customer support. " +
                    "I cannot answer general questions, provide information about other AI systems, " +
                    "or discuss topics outside our business services.\n\n" +
                    "Please ask about our company, products, services, or customer support " +
                    "and I'll be happy to assist you!";

            displayMessage(rejectionMsg, WARNING_COLOR, true);
            statusLabel.setText("Question redirected to customer service topics");
            return;
        }

        String intent = classifyIntent(userInput);
        getAIResponse(userInput, intent);
    }

    private void handleUndo() {
        if (!undoStack.isEmpty()) {
            String last = undoStack.pop();
            displayMessage("System: Last message undone: \"" + last + "\"",
                    new Color(120, 120, 120), true);
            statusLabel.setText("Last message undone");
        } else {
            displayMessage("System: No messages to undo",
                    new Color(120, 120, 120), true);
        }
    }

    private void handleClear() {
        chatArea.setText("");
        displayWelcomeMessage();
        statusLabel.setText("Chat cleared - Ready for customer service inquiries");
    }

    // Enhanced DSA logic implementations
    private void setupIntents() {
        intentMap.put("refund", "Refund request");
        intentMap.put("payment", "Payment issue");
        intentMap.put("order", "Order tracking");
        intentMap.put("cancel", "Cancellation");
        intentMap.put("login", "Account issue");
        intentMap.put("price", "Pricing inquiry");
        intentMap.put("help", "General support");
        intentMap.put("service", "Service information");
        intentMap.put("company", "Company details");
        intentMap.put("product", "Product information");
        intentMap.put("shipping", "Shipping inquiry");
        intentMap.put("return", "Return policy");
        intentMap.put("warranty", "Warranty information");
        intentMap.put("technical", "Technical support");
        intentMap.put("billing", "Billing inquiry");
        intentMap.put("account", "Account management");
        intentMap.put("support", "Customer support");
        intentMap.put("delivery", "Delivery status");
        intentMap.put("policy", "Company policy");
        intentMap.put("track", "Order tracking");
        intentMap.put("complaint", "Customer complaint");
    }

    private void buildTrie() {
        for (String word : intentMap.keySet()) {
            insertTrie(word);
        }
    }

    private void insertTrie(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }
        node.isEnd = true;
    }

    private String classifyIntent(String input) {
        input = input.toLowerCase();
        for (String key : intentMap.keySet()) {
            if (kmpSearch(key, input))
                return intentMap.get(key);
        }
        return "Customer service inquiry";
    }

    private boolean kmpSearch(String pattern, String text) {
        if (pattern.length() > text.length())
            return false;

        int[] lps = computeLPS(pattern);
        int i = 0, j = 0;
        while (i < text.length()) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i++;
                j++;
                if (j == pattern.length())
                    return true;
            } else if (j != 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return false;
    }

    private int[] computeLPS(String pattern) {
        int[] lps = new int[pattern.length()];
        int len = 0, i = 1;
        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else if (len != 0) {
                len = lps[len - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }
        return lps;
    }

    // Enhanced irrelevant detection - STRICT customer service only
    private boolean isIrrelevant(String input) {
        String lower = input.toLowerCase();

        // Comprehensive list of forbidden topics
        String[] unrelated = {
                // Other AI systems
                "chatgpt", "deepseek", "gpt", "gemini", "claude", "ai model", "llama",
                "openai", "anthropic", "google ai", "meta ai", "artificial intelligence",

                // General knowledge
                "prime minister", "president", "capital", "weather", "sports", "movie",
                "music", "celebrity", "history", "politics", "religion", "science",
                "math", "physics", "chemistry", "biology", "astronomy", "geography",

                // Daily life
                "recipe", "cooking", "travel", "vacation", "holiday", "school",
                "teacher", "university", "game", "sports", "entertainment",

                // Technology (general)
                "programming", "coding", "software", "hardware", "computer", "phone",
                "internet", "website", "app development",

                // Personal
                "how are you", "your name", "who made you", "who created you",
                "what can you do", "tell me about yourself",

                // Philosophy and abstract
                "meaning of life", "philosophy", "opinion", "thoughts on",

                // Time and date
                "time", "date", "year", "month", "day"
        };

        for (String w : unrelated) {
            if (lower.contains(w))
                return true;
        }

        // Additional pattern matching for AI-related questions
        if (lower.matches(".*(what|who|how).*(ai|chatbot|assistant|model).*")) {
            return true;
        }

        return false;
    }

    // Get AI Response from OpenRouter API with STRICT customer service focus
    private void getAIResponse(String userInput, String intent) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                // STRICT system message - customer service only
                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", "You are a professional customer service assistant for a company. " +
                        "STRICTLY follow these rules:\n" +
                        "1. ONLY answer questions about the company, its products, services, and customer support\n" +
                        "2. NEVER answer questions about other AI systems, general knowledge, or unrelated topics\n" +
                        "3. If asked about ChatGPT, DeepSeek, Gemini, or any other AI, politely decline and redirect to company services\n"
                        +
                        "4. Keep responses professional, concise, and helpful\n" +
                        "5. Focus on: order tracking, payments, refunds, account issues, product info, technical support\n"
                        +
                        "6. If unsure, ask for clarification about company-related matters only\n\n" +
                        "Current intent category: " + intent + "\n" +
                        "User question: " + userInput);

                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", userInput);

                JSONArray messages = new JSONArray();
                messages.put(systemMessage);
                messages.put(userMessage);

                JSONObject json = new JSONObject();
                json.put("model", CurrentModel);
                json.put("messages", messages);
                json.put("max_tokens", 500);
                json.put("temperature", 0.3);

                RequestBody body = RequestBody.create(
                        json.toString(),
                        MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(API_URL)
                        .addHeader("Authorization", "Bearer " + API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("HTTP-Referer", "http://localhost:8080")
                        .addHeader("X-Title", "Customer Service AI - Professional Support")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String responseData = response.body().string();

                if (!response.isSuccessful()) {
                    throw new IOException("API request failed: " + response.code() + " - " + responseData);
                }

                JSONObject jsonResponse = new JSONObject(responseData);
                String reply = jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim();

                // Remove ** markdown formatting from the response
                final String cleanReply = reply.replaceAll("\\*\\*", "");

                SwingUtilities.invokeLater(() -> {
                    displayMessage("Customer Service: " + cleanReply, PRIMARY_COLOR, true);
                    statusLabel.setText("Response received via " + CurrentModel);
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    displayMessage("System Error: " + e.getMessage(), WARNING_COLOR, true);
                    statusLabel.setText("Error - please try again");
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void displayMessage(String msg, Color color, boolean isAI) {
        // Create styled message
        if (isAI) {
            chatArea.setForeground(color);
            chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        } else {
            chatArea.setForeground(color);
            chatArea.setFont(new Font("Segoe UI", Font.BOLD, 14));
        }

        chatArea.append(msg + "\n\n");

        // Auto-scroll to bottom
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd;
    }

    public static void main(String[] args) {
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new ConversationalAI_DSA().setVisible(true);
        });
    }

    // TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
    // click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
    public static class Main {
        public static void main(String[] args) {
            // TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the
            // highlighted text
            // to see how IntelliJ IDEA suggests fixing it.
            System.out.print("Hello and welcome!");

            for (int i = 1; i <= 5; i++) {
                // TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have
                // set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
                // for you, but you can always add more by pressing <shortcut
                // actionId="ToggleLineBreakpoint"/>.
                System.out.println("i = " + i);
            }
        }
    }
}