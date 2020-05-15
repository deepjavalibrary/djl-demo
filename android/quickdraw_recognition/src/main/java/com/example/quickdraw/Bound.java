package com.example.quickdraw;

import android.graphics.Path;
import android.graphics.RectF;

public class Bound {
    private RectF bound;

    public Bound() {
        bound = new RectF(Float.MAX_VALUE, Float.MAX_VALUE, 0.0f, 0.0f);
    }

    public RectF getBound() {
        return bound;
    }

    public void add(Path path) {
        RectF bound = new RectF();
        path.computeBounds(bound, true);
        add(bound);
    }

    public void add(Bound bound) {
        add(bound.getBound());
    }

    public void add(RectF rect) {
        this.bound.left = Math.min(this.bound.left, rect.left);
        this.bound.right = Math.max(this.bound.right, rect.right);
        this.bound.top = Math.min(this.bound.top, rect.top);
        this.bound.bottom = Math.max(this.bound.bottom, rect.bottom);
    }
}
