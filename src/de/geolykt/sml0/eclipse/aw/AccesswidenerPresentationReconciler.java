package de.geolykt.sml0.eclipse.aw;

import java.util.Locale;

import javax.inject.Inject;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

public class AccesswidenerPresentationReconciler extends PresentationReconciler {

    public AccesswidenerPresentationReconciler() {
        RuleBasedScanner scanner = new RuleBasedScanner();
        scanner.setRules(new AWRule());
        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
        this.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        this.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
    }
} class AWRule implements IRule {

    private StringBuffer buffer = new StringBuffer();
    @Inject IThemeManager themeManager;

    private static boolean colorsSetup = false;
    private static boolean propertyChangeListenerSetup = false;
    private static TextAttribute accessTagAttribute;
    private static TextAttribute scopeTagAttribute;
    private static TextAttribute commentTagAttribute;
    private static TextAttribute headerTagAttribute;
    private static TextAttribute textTagAttribute;

    @Override
    public IToken evaluate(ICharacterScanner scanner) {
        if (themeManager == null) {
            themeManager = PlatformUI.getWorkbench().getThemeManager();
        }
        if (!colorsSetup) {
            ITheme currentTheme = themeManager.getCurrentTheme();
            if (!propertyChangeListenerSetup) {
                propertyChangeListenerSetup = true;
                currentTheme.addPropertyChangeListener(event -> {
                    if (event.getProperty().startsWith("de.geolykt.sml0.preferences.colors.aw.")) {
                        colorsSetup = false;
                    }
                });
            }
            ColorRegistry colorRegistry = currentTheme.getColorRegistry();
            Color color = colorRegistry.get("de.geolykt.sml0.preferences.colors.aw.access");
            accessTagAttribute = new TextAttribute(color);
            color = colorRegistry.get("de.geolykt.sml0.preferences.colors.aw.scope");
            scopeTagAttribute = new TextAttribute(color);
            color = colorRegistry.get("de.geolykt.sml0.preferences.colors.aw.comment");
            commentTagAttribute = new TextAttribute(color);
            color = colorRegistry.get("de.geolykt.sml0.preferences.colors.aw.header");
            headerTagAttribute = new TextAttribute(color);
            color = colorRegistry.get("de.geolykt.sml0.preferences.colors.aw.text");
            textTagAttribute = new TextAttribute(color);

            colorsSetup = true;
        }
        buffer.setLength(0);
        int character = scanner.read();
        if (character == ICharacterScanner.EOF) {
            return Token.UNDEFINED;
        }
        for (; character != ICharacterScanner.EOF; character = scanner.read()) {
            if (character != ' ' && character != '\t' && character != '\n') {
                break;
            }
        }
        boolean isComment = character == '#';
        for (; character != ICharacterScanner.EOF; character = scanner.read()) {
            if (!isComment && (character == ' ' || character == '\t' || character == '#')) {
                scanner.unread();
                break;
            }
            if (character == '\n') {
                scanner.unread();
                break;
            }
            buffer.appendCodePoint(character);
        }
        switch (buffer.toString().toLowerCase(Locale.ROOT)) {
        case "field":
        case "method":
        case "class":
            return new Token(scopeTagAttribute);
        case "accessible":
        case "extendable":
        case "mutable":
        case "natural":
        case "denumerised":
            return new Token(accessTagAttribute);
        case "accesswidener":
            while (character != ICharacterScanner.EOF && character != '\n') {
                character = scanner.read();
            }
            if (character != ICharacterScanner.EOF) {
                scanner.unread();
            }
            return new Token(headerTagAttribute);
        }
        if (isComment) {
            return new Token(commentTagAttribute);
        }
        return buffer.length() != 0 ? new Token(textTagAttribute) : Token.UNDEFINED;
    }
}
