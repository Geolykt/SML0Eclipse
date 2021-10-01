package de.geolykt.sml0.eclipse.aw;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.reconciler.*;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

public class AccesswidenerReconcilerStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {
    private IDocument document;
    private String oldDocument;
    @SuppressWarnings("unused") // For a reason beyond me this has to be kept in storage
    private ProjectionViewer projectionViewer;

    @Override
    public void setDocument(IDocument document) {
        this.document = document;
    }

    public void setProjectionViewer(ProjectionViewer projectionViewer) {
        this.projectionViewer = projectionViewer;
    }

    @Override
    public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
        initialReconcile();
    }

    @Override
    public void reconcile(IRegion partition) {
        initialReconcile();
    }

    @Override
    public void initialReconcile() {
        if (document.get().equals(oldDocument))
            return;
        oldDocument = document.get();
    }

    @Override
    public void setProgressMonitor(IProgressMonitor monitor) {
        // no progress monitor used
    }

}