package handler;

import datatypes.Vector3;

public class CollisionHandler {

    public boolean checkCameraTriangleCollision(Vector3 a, Vector3 b, Vector3 c, String axis, Vector3 tempCoords, double radius) {
        double radiusSq = radius * radius;

        // 1. Edge vectors and vector to camera
        double abX = b.x - a.x, abY = b.y - a.y, abZ = b.z - a.z;
        double acX = c.x - a.x, acY = c.y - a.y, acZ = c.z - a.z;
        double apX = tempCoords.x - a.x, apY = tempCoords.y - a.y, apZ = tempCoords.z - a.z;

        // 2. Unnormalized normal
        double nX = abY * acZ - abZ * acY;
        double nY = abZ * acX - abX * acZ;
        double nZ = abX * acY - abY * acX;

        double nLenSq = nX * nX + nY * nY + nZ * nZ;
        if (nLenSq < 1e-9) return false; // Degenerate triangle

        // Filter by the requested axis (ignore surfaces not facing this axis)
        double nLen = Math.sqrt(nLenSq);
        if (axis != null) {
            if (axis.equalsIgnoreCase("X") && Math.abs(nX / nLen) < 0.2) return false;
            if (axis.equalsIgnoreCase("Y") && Math.abs(nY / nLen) < 0.2) return false;
            if (axis.equalsIgnoreCase("Z") && Math.abs(nZ / nLen) < 0.2) return false;
        }

        // 3. Distance to plane (Keep calculation, REMOVE the strict early-exit return false)
        double distToPlane = (apX * nX + apY * nY + apZ * nZ) / nLen;

        // 4. Find the closest point on triangle via Barycentric Coordinates
        double d1 = abX * apX + abY * apY + abZ * apZ;
        double d2 = acX * apX + acY * apY + acZ * apZ;

        // Vertex A region
        if (d1 <= 0 && d2 <= 0) {
            return (apX * apX + apY * apY + apZ * apZ) < radiusSq;
        }

        // Vertex B region
        double bpX = tempCoords.x - b.x, bpY = tempCoords.y - b.y, bpZ = tempCoords.z - b.z;
        double d3 = abX * bpX + abY * bpY + abZ * bpZ;
        double d4 = acX * bpX + acY * bpY + acZ * bpZ;
        if (d3 >= 0 && d4 <= d3) {
            return (bpX * bpX + bpY * bpY + bpZ * bpZ) < radiusSq;
        }

        // Edge AB region
        double vc = d1 * d4 - d3 * d2;
        if (vc <= 0 && d1 >= 0 && d3 <= 0) {
            double v = d1 / (d1 - d3);
            double distX = tempCoords.x - (a.x + abX * v);
            double distY = tempCoords.y - (a.y + abY * v);
            double distZ = tempCoords.z - (a.z + abZ * v);
            return (distX * distX + distY * distY + distZ * distZ) < radiusSq;
        }

        // Vertex C region
        double cpX = tempCoords.x - c.x, cpY = tempCoords.y - c.y, cpZ = tempCoords.z - c.z;
        double d5 = abX * cpX + abY * cpY + abZ * cpZ;
        double d6 = acX * cpX + acY * cpY + acZ * cpZ;
        if (d6 >= 0 && d5 <= d6) {
            return (cpX * cpX + cpY * cpY + cpZ * cpZ) < radiusSq;
        }

        // Edge AC region
        double vb = d5 * d2 - d1 * d6;
        if (vb <= 0 && d2 >= 0 && d6 <= 0) {
            double w = d2 / (d2 - d6);
            double distX = tempCoords.x - (a.x + acX * w);
            double distY = tempCoords.y - (a.y + acY * w);
            double distZ = tempCoords.z - (a.z + acZ * w);
            return (distX * distX + distY * distY + distZ * distZ) < radiusSq;
        }

        // Edge BC region
        double va = d3 * d6 - d5 * d4;
        if (va <= 0 && (d4 - d3) >= 0 && (d5 - d6) >= 0) {
            double w = (d4 - d3) / ((d4 - d3) + (d5 - d6));
            double distX = tempCoords.x - (b.x + (c.x - b.x) * w);
            double distY = tempCoords.y - (b.y + (c.y - b.y) * w);
            double distZ = tempCoords.z - (b.z + (c.z - b.z) * w);
            return (distX * distX + distY * distY + distZ * distZ) < radiusSq;
        }

        // 5. Face Region (Inside the triangle)
        return Math.abs(distToPlane) < radius;
    }
}
