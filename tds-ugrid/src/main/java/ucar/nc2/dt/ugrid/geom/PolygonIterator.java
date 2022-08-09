/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * PolygonIterator.java
 *
 * Created on May 12, 2009 @ 1:04:32 PM
 */

package ucar.nc2.dt.ugrid.geom;


import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;


/**
 * An iterator over Polygon2D. This class is private to this package.
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class PolygonIterator implements PathIterator {
  /**
   * The transformed coordinates being iterated.
   */
  private double[] _coords;

  /**
   * The current coordinate index.
   */
  private int _index = 0;

  /**
   * The flag saying we are already done
   */
  private boolean _done = false;

  /**
   * Create a new iterator over the given polygon and with the given transform. If the transform is null, that is taken
   * to be the same as
   * a unit Transform.
   */
  public PolygonIterator(Polygon2D pl, AffineTransform at) {
    int count = pl.getVertexCount() * 2;
    _coords = new double[count];

    if (pl instanceof Polygon2D.Float) {
      Polygon2D.Float f = (Polygon2D.Float) pl;

      if ((at == null) || at.isIdentity()) {
        for (int i = 0; i < count; i++) {
          _coords[i] = (double) f._coords[i];
        }
      } else {
        at.transform(f._coords, 0, _coords, 0, count / 2);
      }
    } else {
      Polygon2D.Double d = (Polygon2D.Double) pl;

      if ((at == null) || at.isIdentity()) {
        System.arraycopy(d._coords, 0, _coords, 0, count);
      } else {
        at.transform(d._coords, 0, _coords, 0, count / 2);
      }
    }
  }

  /**
   * Get the current segment
   */
  public int currentSegment(double[] coords) {
    if (_index == _coords.length) {
      if (_done) {
        return PathIterator.SEG_CLOSE;
      } else {
        coords[0] = this._coords[0];
        coords[1] = this._coords[1];
        return PathIterator.SEG_LINETO;
      }
    } else {
      coords[0] = this._coords[_index];
      coords[1] = this._coords[_index + 1];

      if (_index == 0) {
        return PathIterator.SEG_MOVETO;
      } else {
        return PathIterator.SEG_LINETO;
      }
    }
  }

  /**
   * Get the current segment
   */
  public int currentSegment(float[] coords) {
    if (_index == _coords.length) {
      if (_done) {
        return PathIterator.SEG_CLOSE;
      } else {
        coords[0] = (float) this._coords[0];
        coords[1] = (float) this._coords[1];
        return PathIterator.SEG_LINETO;
      }
    } else {
      coords[0] = (float) this._coords[_index];
      coords[1] = (float) this._coords[_index + 1];

      if (_index == 0) {
        return PathIterator.SEG_MOVETO;
      } else {
        return PathIterator.SEG_LINETO;
      }
    }
  }

  /**
   * Return the winding rule. This is WIND_NON_ZERO.
   */
  public int getWindingRule() {
    return PathIterator.WIND_NON_ZERO;
  }

  /**
   * Test if the iterator is done.
   */
  public boolean isDone() {
    return _done;
  }

  /**
   * Move the iterator along by one point.
   */
  public void next() {
    if (_index == _coords.length) {
      _done = true;
    } else {
      _index += 2;
    }
  }
}
