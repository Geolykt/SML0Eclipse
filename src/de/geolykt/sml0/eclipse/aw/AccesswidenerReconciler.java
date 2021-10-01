package de.geolykt.sml0.eclipse.aw;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.reconciler.Reconciler;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

public class AccesswidenerReconciler extends Reconciler {

    private AccesswidenerReconcilerStrategy fStrategy;

    public AccesswidenerReconciler() {
        // TODO this is logic for .project file to fold tags. Replace with your language
        // logic!
        fStrategy = new AccesswidenerReconcilerStrategy();
        this.setReconcilingStrategy(fStrategy, IDocument.DEFAULT_CONTENT_TYPE);
    }

    @Override
    public void install(ITextViewer textViewer) {
        super.install(textViewer);
        ProjectionViewer pViewer = (ProjectionViewer) textViewer;
        fStrategy.setProjectionViewer(pViewer);
    }
}