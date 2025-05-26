import * as THREE from 'three';

export class LeonMath {

    /**
     * Calculates the intersection of a moving sphere with a static plane.
     * @param {number} sphereRadius - Radius of the sphere.
     * @param {THREE.Vector3} sphereCenter - Initial center of the sphere.
     * @param {THREE.Vector3} sphereVel - Velocity vector of the sphere (movement for one frame/step).
     * @param {THREE.Vector3} planeNormal - Normal vector of the plane.
     * @param {number} planeD - D constant of the plane equation (N dot P = D). Normal points away from collidable side.
     * @param {number[]} outScalar - Array to store the time of collision (t), where 0 <= t <= 1.
     * @param {THREE.Vector3} outCollisionPoint - Vector to store the exact point of collision on the sphere's surface.
     * @returns {boolean} - True if an intersection occurs within the movement segment, false otherwise.
     */
    static collisionSphereVelPlane(sphereRadius, sphereCenter, sphereVel, planeNormal, planeD, outScalar, outCollisionPoint) {
        outScalar[0] = Infinity;

        // Distance from sphere center to plane at t=0
        const distToPlane = planeNormal.dot(sphereCenter) - planeD;

        // Speed of sphere towards plane
        const speedTowardsPlane = planeNormal.dot(sphereVel);

        // If sphere is moving parallel or away from the plane
        if (speedTowardsPlane >= 0 && Math.abs(distToPlane) > sphereRadius) { // Also check if already interpenetrating but moving away
             // Check if initially interpenetrating
            if (distToPlane < sphereRadius && distToPlane > -sphereRadius) { // Allow for slight interpenetration
                 // If already interpenetrating, consider t=0 if moving into it or parallel but still interpenetrating
                if (speedTowardsPlane <= 0) { // Moving into it or parallel
                    outScalar[0] = 0;
                     // Collision point is center projected on plane, then moved back by radius along normal
                    const centerOnPlane = sphereCenter.clone().addScaledVector(planeNormal, -distToPlane);
                    outCollisionPoint.copy(centerOnPlane).addScaledVector(planeNormal, sphereRadius); // Point on sphere surface
                    return true;
                }
            }
            return false;
        }


        // Time to reach plane with sphere's surface (center has to travel distToPlane - radius)
        let t0;
        if (distToPlane > sphereRadius) { // Approaching from outside
            t0 = (distToPlane - sphereRadius) / -speedTowardsPlane;
        } else if (distToPlane < -sphereRadius) { // Moving away from inside (should not happen if plane is one-sided)
             return false; // No collision if moving away from "behind" the plane.
        } else { // Center is already within radius distance or interpenetrating
            t0 = 0; // Already colliding or very close
        }
        
        // Check if collision occurs within the current velocity segment (0 <= t <= 1)
        if (t0 >= 0 && t0 <= 1) {
            outScalar[0] = t0;
            // Calculate collision point on sphere surface
            // Point on sphere center path at time t0
            const collisionCenter = sphereCenter.clone().addScaledVector(sphereVel, t0);
            // Project this center onto the plane, then move back by radius along sphere's approach to plane
            // Or, simply, the collision point on the plane is collisionCenter projected,
            // and on the sphere it's that point moved by radius towards sphere center.
            outCollisionPoint.copy(collisionCenter).addScaledVector(planeNormal, -sphereRadius);
            return true;
        }
        return false;
    }


    /**
     * Calculates the intersection of a moving sphere with a static sphere.
     * Based on https://gamedev.stackexchange.com/a/97815 and other geometric solutions for quadratic equation.
     * @param {number} s1Rad - Radius of the moving sphere.
     * @param {THREE.Vector3} s1Center - Initial center of the moving sphere.
     * @param {THREE.Vector3} s1Vel - Velocity vector of the moving sphere.
     * @param {number} s2Rad - Radius of the static sphere.
     * @param {THREE.Vector3} s2Center - Center of the static sphere.
     * @param {number[]} outScalar - Array to store the time of collision (t), where 0 <= t <= 1.
     * @returns {boolean} - True if an intersection occurs within the movement segment, false otherwise.
     */
    static collisionSphereVelSphere(s1Rad, s1Center, s1Vel, s2Rad, s2Center, outScalar) {
        outScalar[0] = Infinity;

        const totalRadius = s1Rad + s2Rad;
        const relCenter = s1Center.clone().sub(s2Center); // Vector from s2Center to s1Center

        // Coefficients for the quadratic equation: a*t^2 + b*t + c = 0
        const a = s1Vel.dot(s1Vel);
        const b = 2 * relCenter.dot(s1Vel);
        const c = relCenter.dot(relCenter) - totalRadius * totalRadius;

        // If sphere1 is already intersecting sphere2 at t=0
        if (c <= 0) {
            // Check if moving towards each other or already overlapping and not separating fast enough
            if (b < 0) { // If b < 0, they are moving towards each other or overlapping
              outScalar[0] = 0;
              return true;
            }
            // If c <= 0 and b >=0, they are already intersecting but moving apart or stationary relative to each other.
            // We might still want to report t=0 if deeply interpenetrating.
            // For simplicity, if already touching/intersecting (c<=0) and moving towards (b<0), it's a collision at t=0.
            // If c <= 0 and b >= 0, they are separating or static, no "new" collision along vel.
            // However, game logic might need to handle initial interpenetration.
            // Let's stick to: collision if c <=0 and moving towards each other.
            // If we want to catch all initial overlaps, just `if (c <= 0) { outScalar[0] = 0; return true; }`
            // but this might cause issues if they are already moving apart.
            // The original Java likely assumes non-interpenetration at start of tick or handles it.
            // For now, let's say if c <= 0 and b < 0 (moving towards), t=0.
            // If just c <= 0 (already overlapping), it implies an immediate collision.
             outScalar[0] = 0;
             return true; // Simpler: if already overlapping, it's a collision.
        }

        // Calculate discriminant
        const discriminant = b * b - 4 * a * c;

        // If discriminant is negative, no real roots, so no collision
        if (discriminant < 0) {
            return false;
        }

        // Calculate the two potential times of collision
        const t0 = (-b - Math.sqrt(discriminant)) / (2 * a);
        // const t1 = (-b + Math.sqrt(discriminant)) / (2 * a); // Time of exit, not usually needed for first contact

        if (t0 >= 0 && t0 <= 1) {
            outScalar[0] = t0;
            return true;
        }

        return false;
    }
}
