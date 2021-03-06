/*******************************************************************************************************
 *
 * msi.gama.metamodel.topology.grid.GridHexagonalNeighborhoodVertical.java, in plugin msi.gama.core, is part of the
 * source code of the GAMA modeling and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.metamodel.topology.grid;

import java.util.HashSet;
import java.util.Set;

public class GridHexagonalNeighborhoodVertical extends GridHexagonalNeighborhood {

	/**
	 * @param gamaSpatialMatrix
	 */
	GridHexagonalNeighborhoodVertical(final GamaSpatialMatrix matrix) {
		super(matrix);
	}

	@Override
	public Set<Integer> getNeighborsAtRadius1(final int placeIndex, final int xSize, final int ySize,
			final boolean isTorus) {
		final int y = placeIndex / xSize;
		final int x = placeIndex - y * xSize;
		final Set<Integer> neigh = new HashSet<>();
		int id = getIndexAt(x, y - 1, xSize, ySize, isTorus);
		if (id != -1) {
			neigh.add(id);
		}
		id = getIndexAt(x, y + 1, xSize, ySize, isTorus);
		if (id != -1) {
			neigh.add(id);
		}
		id = getIndexAt(x - 1, y, xSize, ySize, isTorus);
		if (id != -1) {
			neigh.add(id);
		}
		id = getIndexAt(x + 1, y, xSize, ySize, isTorus);
		if (id != -1) {
			neigh.add(id);
		}
		if (y % 2 != 0) {
			id = getIndexAt(x + 1, y - 1, xSize, ySize, isTorus);
			if (id != -1) {
				neigh.add(id);
			}
			id = getIndexAt(x + 1, y + 1, xSize, ySize, isTorus);
			if (id != -1) {
				neigh.add(id);
			}
		} else {
			id = getIndexAt(x - 1, y - 1, xSize, ySize, isTorus);
			if (id != -1) {
				neigh.add(id);
			}
			id = getIndexAt(x - 1, y + 1, xSize, ySize, isTorus);
			if (id != -1) {
				neigh.add(id);
			}
		}
		return neigh;
	}

}