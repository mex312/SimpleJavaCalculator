package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.font.TextLayout;
import java.util.ArrayList;

public class GUI {
    private JPanel root;
    private CustomJButton plusButton;
    private CustomJButton minusButton;
    private CustomJButton multiplyButton;
    private CustomJButton equalsButton;
    private CustomJButton divideButton;
    private CustomJButton backspaceButton;
    private CustomJButton percentButton;
    private CustomJButton clearButton;
    private CustomJButton eraseButton;
    private CustomJButton oneOverXButton;
    private CustomJButton powButton;
    private CustomJButton negateButton;
    private CustomJButton n0Button;
    private CustomJButton dotButton;
    private CustomJButton n1Button;
    private CustomJButton n2Button;
    private CustomJButton n3Button;
    private CustomJButton n4Button;
    private CustomJButton n5Button;
    private CustomJButton n6Button;
    private CustomJButton n7Button;
    private CustomJButton n8Button;
    private CustomJButton n9Button;
    private JLabel field;
    private JLabel upField;
    private JPanel fieldPanel;
    private CustomJButton sqrtButton;


    // Not my code :P
    public Font scaleFont(
            final JLabel label, final Rectangle dst, final Graphics graphics ) {
        assert label != null;
        assert dst != null;
        assert graphics != null;

//        dst.y -= 2;
//        dst.x -= 4;

        final var font = label.getFont();
        final var text = label.getText();

        final var frc = ((Graphics2D) graphics).getFontRenderContext();

        final var dstWidthPx = dst.getWidth();
        final var dstHeightPx = dst.getHeight();

        var minSizePt = 1f;
        var maxSizePt = 1000f;
        var scaledFont = font;
        float scaledPt = scaledFont.getSize();

        while( maxSizePt - minSizePt > 1f ) {
            scaledFont = scaledFont.deriveFont( scaledPt );

            final var layout = new TextLayout( text, scaledFont, frc );
            final var fontWidthPx = layout.getVisibleAdvance();

            final var metrics = scaledFont.getLineMetrics( text, frc );
            final var fontHeightPx = metrics.getHeight();

            if( (fontWidthPx > dstWidthPx) || (fontHeightPx > dstHeightPx) ) {
                maxSizePt = scaledPt;
            }
            else {
                minSizePt = scaledPt;
            }

            scaledPt = (minSizePt + maxSizePt) / 2;
        }

        return scaledFont.deriveFont( (float) Math.floor( scaledPt ) );
    }


    void setFieldText(String text) {
        field.setText(text);
        field.setFont(scaleFont(field, new Rectangle(fieldPanel.getWidth() - 20, Math.min(fieldPanel.getHeight(), 75)), field.getGraphics()));
    }


    enum Function {
        SQR,
        SQRT,
        INVERSE,
        NEGATE
    }

    static void wrapWith(StringBuilder value, Function function) {
        switch (function) {
            case SQR -> {
                value.insert(0, '(');
                value.append(")^2");
            }
            case SQRT -> {
                value.insert(0, "sqrt(");
                value.append(')');
            }
            case INVERSE -> {
                value.insert(0, "1/(");
                value.append(')');
            }
            case NEGATE -> {
                value.insert(0, "-(");
                value.append(')');
            }
        }
    }

    static double applyTo(double value, Function function) {
        return switch (function) {
            case SQR -> value * value;
            case SQRT -> Math.sqrt(value);
            case INVERSE -> 1.0 / value;
            case NEGATE -> -value;
            default -> value;
        };
    }

    static class Value {
        Double number;
        ArrayList<Function> functions = new ArrayList<>();

        public Value(double number) {
            this.number = number;
        }

        public void addFunction(Function func) {
            functions.add(func);
        }

        String render() {
            StringBuilder out = new StringBuilder(String.valueOf(number));

            for(var function : functions) {
                wrapWith(out, function);
            }

            return out.toString();
        }

        double calculate() {
            double out = number;

            for(var function : functions) {
                out = applyTo(out, function);
            }

            return out;
        }
    }

    static class HistoryItem {
        Value left = null;
        Value right = null;
        Character operation = null;

        public String render() {
            if(left == null) return "0";
            if(operation == null) return left.render();
            if(right == null) return left.render() + " " + operation;
            return left.render() + " " + operation + " " + right.render();
        }

        public double calculate() {
            if(left == null) {
                return 0;
            } else if(right == null) {
                return left.calculate();
            } else {
                double left = this.left.calculate();
                double right = this.right.calculate();

                return switch (operation) {
                    case '+' -> left + right;
                    case '-' -> left - right;
                    case '*' -> left * right;
                    case '/' -> left / right;
                    default -> throw new IllegalStateException("Unexpected value: " + operation);
                };
            }
        }
    }


    private String currNumber = "";
    private HistoryItem currItem = new HistoryItem();



    String removeLeadingZeros(String str) {
        while(str.startsWith("0")) {
            str = str.substring(1);
        }
        if(str.startsWith(".") || str.isEmpty()) str = "0" + str;

        return str;
    }


    public boolean appendToField(String str) {
        String text = currNumber + str;

        try {
            Double.parseDouble(text);
            currNumber = removeLeadingZeros(text);
            setFieldText(!currNumber.isEmpty() ? currNumber : "0");
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public void backspaceField() {
        if(currNumber.isEmpty()) currNumber = "0";
        else currNumber = currNumber.substring(0, currNumber.length() - 1);
        setFieldText(!currNumber.isEmpty() ? currNumber : "0");
    }



    private void applyOperation(char operation) {
        if(currItem.left == null) {
            if (currNumber.isEmpty())
                currItem.left = new Value(0);
            else
                currItem.left = new Value(Double.parseDouble(currNumber));

            currItem.operation = operation;

            currNumber = "";
        } else if(currItem.operation == null) {
            currItem.operation = operation;
            if(!currNumber.isEmpty()) if(currItem.left.calculate() != Double.parseDouble(currNumber)) currItem.left = new Value(Double.parseDouble(currNumber));
        } else if(currItem.right == null) {
            if(currNumber.isEmpty()) {
                currItem.operation = operation;
            } else {
                currItem.right = new Value(Double.parseDouble(currNumber));
                currItem.left = new Value(currItem.calculate());
                currItem.right = null;
                currItem.operation = operation;
                currNumber = "";
                setFieldText(String.valueOf(currItem.left.calculate()));
            }
        }

        renderUpField(false);
    }

    private void applyFunction(Function function) {
        if (currItem.left == null) {
            if(currNumber.isEmpty())
                currItem.left = new Value(0);
            else
                currItem.left = new Value(Double.parseDouble(currNumber));
            currItem.left.functions.add(function);
            currNumber = String.valueOf(currItem.left.calculate());
        } else {
            if (currItem.operation == null) {
                currItem.left.functions.add(function);
                currNumber = String.valueOf(currItem.left.calculate());
            } else {
                if (currItem.right == null) {
                    if(currNumber.isEmpty())
                        currItem.right = new Value(currItem.left.calculate());
                    else
                        currItem.right = new Value(Double.parseDouble(currNumber));
                }
                currItem.right.functions.add(function);
                currNumber = String.valueOf(currItem.right.calculate());
            }
        }

        renderField();
        renderUpField(false);

        currNumber = "";
    }

    private void applyPercent() {
        if(currItem.left == null) {
            if(currNumber.isEmpty())
                currItem.left = new Value(0);
            else
                currItem.left = new Value(Double.parseDouble(currNumber) / 100.0);

            currNumber = String.valueOf(currItem.left.calculate());
        } else if(currItem.operation == null) {
            currItem.left = new Value(currItem.left.calculate() / 100.0);
            currNumber = String.valueOf(currItem.left.calculate());
        } else if(currItem.right == null) {
            if(currNumber.isEmpty())
                currItem.right = new Value(0);
            else
                currItem.right = new Value(Double.parseDouble(currNumber) / 100.0);

            currNumber = String.valueOf(currItem.right.calculate());
        } else {
            currItem.right = new Value(currItem.right.calculate() / 100.0);
            currNumber = String.valueOf(currItem.right.calculate());
        }

        renderField();
        renderUpField(false);
        currNumber = "";
    }

    private void applyNegation() {
        if(!currNumber.isEmpty()) {
            currNumber = String.valueOf(-Double.parseDouble(currNumber));
            renderField();
        } else {
            applyFunction(Function.NEGATE);
        }
    }

    private void calculate() {
        if(currItem.left == null) {
            if(currNumber.isEmpty())
                currItem.left = new Value(0);
            else
                currItem.left = new Value(Double.parseDouble(currNumber));
        } else if(currItem.operation != null) {
            if (currItem.right == null) {
                if (currNumber.isEmpty())
                    currItem.right = new Value(currItem.left.number);
                else
                    currItem.right = new Value(Double.parseDouble(currNumber));
            }
        }

        currNumber = String.valueOf(currItem.calculate());

        renderField();
        renderUpField(true);

        currItem.left = new Value(currItem.calculate());
        currItem.right = null;
        currItem.operation = null;

        currNumber = "";
    }


    public JPanel getRoot() {
        return root;
    }


    void renderField() {
        setFieldText(currNumber.isEmpty() ? "0" : currNumber);
    }

    void renderUpField(boolean withEquals) {
        if(currItem != null) {
            this.upField.setText(currItem.render());
        }


        if(withEquals) this.upField.setText(upField.getText() + '=');
    }


    public void listen(KeyEvent e) {
        System.out.println(e.getKeyChar());

        switch (e.getKeyChar()) {
            case '.', ',' -> dotButton.doClick();
            case '\b' -> backspaceButton.doClick();

            case '+' -> plusButton.doClick();
            case '-' -> minusButton.doClick();
            case '*' -> multiplyButton.doClick();
            case '/' -> divideButton.doClick();
            case '=', '\n' -> equalsButton.doClick();

            case '0' -> n0Button.doClick();
            case '1' -> n1Button.doClick();
            case '2' -> n2Button.doClick();
            case '3' -> n3Button.doClick();
            case '4' -> n4Button.doClick();
            case '5' -> n5Button.doClick();
            case '6' -> n6Button.doClick();
            case '7' -> n7Button.doClick();
            case '8' -> n8Button.doClick();
            case '9' -> n9Button.doClick();
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_S -> powButton.doClick();
            case KeyEvent.VK_R -> sqrtButton.doClick();
            case KeyEvent.VK_O -> oneOverXButton.doClick();
            case KeyEvent.VK_P -> percentButton.doClick();
            case KeyEvent.VK_N -> negateButton.doClick();
        }
    }

    public GUI() {
        backspaceButton.addActionListener(event -> {backspaceField();});
        clearButton.addActionListener(event -> {
            currNumber = "";
            currItem = new HistoryItem();
            renderField();
            renderUpField(false);
        });
        eraseButton.addActionListener(event -> {
            currNumber = "";
            renderField();
        });

        n0Button.addActionListener(event -> {appendToField(event.getActionCommand());});
        n1Button.addActionListener(event -> {appendToField(event.getActionCommand());});
        n2Button.addActionListener(event -> {appendToField(event.getActionCommand());});
        n3Button.addActionListener(event -> {appendToField(event.getActionCommand());});
        n4Button.addActionListener(event -> {appendToField(event.getActionCommand());});
        n5Button.addActionListener(event -> {appendToField(event.getActionCommand());});
        n6Button.addActionListener(event -> {appendToField(event.getActionCommand());});
        n7Button.addActionListener(event -> {appendToField(event.getActionCommand());});
        n8Button.addActionListener(event -> {appendToField(event.getActionCommand());});
        n9Button.addActionListener(event -> {appendToField(event.getActionCommand());});
        dotButton.addActionListener(event -> {appendToField(event.getActionCommand());});

        plusButton.addActionListener(event -> {applyOperation('+');});
        minusButton.addActionListener(event -> {applyOperation('-');});
        multiplyButton.addActionListener(event -> {applyOperation('*');});
        divideButton.addActionListener(event -> {applyOperation('/');});

        powButton.addActionListener(event -> {applyFunction(Function.SQR);});
        sqrtButton.addActionListener(event -> {applyFunction(Function.SQRT);});
        oneOverXButton.addActionListener(event -> {applyFunction(Function.INVERSE);});

        negateButton.addActionListener(event -> {applyNegation();});

        percentButton.addActionListener(event -> {applyPercent();});

        equalsButton.addActionListener(event -> {calculate();});
    }
}
