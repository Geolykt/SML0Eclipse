package de.geolykt.sml0.eclipse.aw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;

public class AWValidator implements IDocumentListener {

    public static final String PROBLEM_HEADER_MISSING = "Header not found.";
    public static final String PROBLEM_HEADER_INVALID = "Header was invalid. It must be in the format \"accessWidener v2 namespace\"";
    public static final String PROBLEM_INVALID_CLASS_MODIFIER = "Only \"accessible\", \"expandable\", \"mutable\" or \"natural\", \"denumerised\" are allowed.";
    public static final String PROBLEM_INVALID_FIELD_MODIFIER = "Only \"accessible\", \"expandable\", \"mutable\" or \"natural\" are allowed.";
    public static final String PROBLEM_INVALID_MEMBER_TOKEN = "Only \"class\", \"field\" or \"method\" are allowed for the second token.";
    public static final String PROBLEM_INVALID_METHOD_MODIFIER = "Only \"accessible\", \"expandable\", \"mutable\" or \"natural\" are allowed.";
    public static final String PROBLEM_NO_MEMBER_TOKEN = "Only a single token was found, at least 3 are needed.";
    public static final String PROBLEM_UNEXPECTED_TOKEN_AMOUNT_CLASS = "Exactly 3 tokens are required for the class scope.";
    public static final String PROBLEM_UNEXPECTED_TOKEN_AMOUNT_FIELD = "Exactly 5 tokens are required for the field scope.";
    public static final String PROBLEM_UNEXPECTED_TOKEN_AMOUNT_METHOD = "Exactly 5 tokens are required for the method scope.";

    protected final IFile file;
    protected final List<IMarker> markers = new ArrayList<>();

    public AWValidator(IFile file) {
        this.file = file;
    }

    protected void addProblem(int severity, String message, int line) {
        try {
            IMarker marker = file.createMarker(IMarker.PROBLEM);
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
            marker.setAttribute(IMarker.MESSAGE, message);
            marker.setAttribute(IMarker.LINE_NUMBER, line);
            markers.add(marker);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    protected boolean classCompatibleAccess(String modifier, int lineNr) {
        switch (modifier.toLowerCase(Locale.ROOT)) {
        case "accessible":
        case "extendable":
        case "mutable":
        case "natural":
        case "denumerised":
            return true;
        default:
            addProblem(IMarker.SEVERITY_ERROR, PROBLEM_INVALID_CLASS_MODIFIER, lineNr);
            return false;
        }
    }

    @Override
    public void documentAboutToBeChanged(DocumentEvent paramDocumentEvent) {
        // The validator can ignore this method as we don't need to validate stuff that is changed anyways in a bit.
    }

    @Override
    public void documentChanged(DocumentEvent event) {
        Iterator<IMarker> oldMarkers = markers.iterator();
        while (oldMarkers.hasNext()) {
            try {
                oldMarkers.next().delete();
                oldMarkers.remove();
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        oldMarkers = null;

        int lineNr = 1;
        try (BufferedReader reader = new BufferedReader(new StringReader(event.getDocument().get()));) {
            String header = reader.readLine();
            if (header == null || header.isBlank()) {
                addProblem(IMarker.SEVERITY_ERROR, PROBLEM_HEADER_MISSING, lineNr);
                reader.close();
                return;
            }
            String[] headerTokens = header.split("\\s+");
            if (headerTokens.length != 3 || !headerTokens[0].equals("accessWidener")
                    || !(headerTokens[1].equals("v1") || headerTokens[1].equals("v2"))) {
                addProblem(IMarker.SEVERITY_ERROR, PROBLEM_HEADER_INVALID, lineNr);
                reader.close();
                return;
            }
            lineNr++;
            lineLoop:
            for (String line = reader.readLine(); line != null; line = reader.readLine(), lineNr++) {
                line = line.split("#", 2)[0];
                if (line.isBlank()) {
                    continue; // comment line
                }
                String[] tokens = line.split("\\s+");
                if (tokens.length == 0) {
                    continue; // shouldn't happen, but just to be sure.
                }
                String modifier = tokens[0];
                if (tokens.length == 1) {
                    addProblem(IMarker.SEVERITY_ERROR, PROBLEM_NO_MEMBER_TOKEN, lineNr);
                    continue;
                }
                String memberType = tokens[1];
                switch (memberType) {
                case "class":
                    if (tokens.length != 3) {
                        addProblem(IMarker.SEVERITY_ERROR, PROBLEM_UNEXPECTED_TOKEN_AMOUNT_CLASS, lineNr);
                        continue lineLoop;
                    }
                    if (!classCompatibleAccess(modifier, lineNr)) {
                        continue lineLoop;
                    }
                    break;
                case "field":
                    if (tokens.length != 5) {
                        addProblem(IMarker.SEVERITY_ERROR, PROBLEM_UNEXPECTED_TOKEN_AMOUNT_FIELD, lineNr);
                        continue lineLoop;
                    }
                    if (!fieldCompatibleAccess(modifier, lineNr)) {
                        continue lineLoop;
                    }
                    break;
                case "method":
                    if (tokens.length != 5) {
                        addProblem(IMarker.SEVERITY_ERROR, PROBLEM_UNEXPECTED_TOKEN_AMOUNT_METHOD, lineNr);
                        continue lineLoop;
                    }
                    if (!methodCompatibleAccess(modifier, lineNr)) {
                        continue lineLoop;
                    }
                    break;
                default:
                    addProblem(IMarker.SEVERITY_ERROR, PROBLEM_INVALID_MEMBER_TOKEN, lineNr);
                    continue lineLoop;
                }
            }
            reader.close();
        } catch (IOException e) {
            addProblem(IMarker.SEVERITY_INFO, e.getLocalizedMessage(), lineNr);
        }
    }

    protected boolean fieldCompatibleAccess(String modifier, int lineNr) {
        switch (modifier.toLowerCase(Locale.ROOT)) {
        case "accessible":
        case "extendable":
        case "mutable":
        case "natural":
            return true;
        default:
            addProblem(IMarker.SEVERITY_ERROR, PROBLEM_INVALID_FIELD_MODIFIER, lineNr);
            return false;
        }
    }

    protected boolean methodCompatibleAccess(String modifier, int lineNr) {
        switch (modifier.toLowerCase(Locale.ROOT)) {
        case "accessible":
        case "extendable":
        case "mutable":
        case "natural":
            return true;
        default:
            addProblem(IMarker.SEVERITY_ERROR, PROBLEM_INVALID_METHOD_MODIFIER, lineNr);
            return false;
        }
    }

}
