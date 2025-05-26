import * as THREE from 'three';
import { Labyrinth } from './Labyrinth.js'; // For Labyrinth.CELLWIDTH etc.
import { Cell } from './Cell.js'; // For Cell types

export class PhysicEntity {
    constructor(labyrinth) {
        this.labyrinth = labyrinth;

        this.position = new THREE.Vector3();
        this.direction = new THREE.Vector3(0, 0, 1); // Forward Z
        this.directionVel = new THREE.Vector3().copy(this.direction); // Velocity direction
        this.perpendicular = new THREE.Vector3(0, 1, 0); // Up vector
        this.vecside = new THREE.Vector3().crossVectors(this.direction, this.perpendicular).normalize(); // Should be (-1,0,0) for (0,0,1) dir

        this.normal = new THREE.Vector3(); // Collision normal
        this.collisionPoint = new THREE.Vector3(); // Point of collision

        this.velocity = new THREE.Vector3();
        this.impulse = 0.0;
        this.friction = 0.08;
        this.angle = 0.0; // Current angular change to apply (delta)
        this.rotationAngle = 0.0; // Accumulated rotation (like in PlayerCamera)
        this.angfriction = 0.05;

        this.radius = 1.0; // Default collision radius
        this.applyfriction = true;
        this.applyanglefriction = true;
        this.applynormal = false; // True if collision occurred

        this.m_cell = null; // Current cell
        // Initialize m_cell based on initial position (e.g., if position is set later)
        // For now, let's assume it will be set by the derived class or after position is known.
        // this.updateCurrentCell(); // Needs position to be set first
    }

    updateCurrentCell() {
        if (!this.labyrinth) return;
        const col = Math.floor(this.position.x / Labyrinth.CELLWIDTH);
        const row = Math.floor(this.position.z / Labyrinth.CELLWIDTH);
        // Determine if upstairs based on Y position, needs careful handling of level heights
        // This is a simplified assumption for Y. A more robust way would be needed if levels overlap in XZ.
        const floorThickness = 0.2;
        const ceilingThickness = 0.2;
        const singleLevelTotalHeight = Labyrinth.CELLHEIGHT + floorThickness + ceilingThickness;
        const upstairs = this.position.y > singleLevelTotalHeight / 2 + floorThickness; // Approx.

        this.m_cell = this.labyrinth.getCell(row, col, upstairs);
        return this.m_cell;
    }

    applyFriction() {
        if (this.applyfriction) {
            this.impulse *= (1.0 - this.friction);
            if (Math.abs(this.impulse) < 0.001) { // Threshold to stop
                this.impulse = 0.0;
            }
        }
    }

    applyAngFriction() {
        if (this.applyanglefriction) {
            this.angle *= (1.0 - this.angfriction);
            if (Math.abs(this.angle) < 0.001) { // Threshold to stop
                this.angle = 0.0;
            }
        }
    }

    // calcRotation is more complex and tied to how 'angle' is used.
    // If 'angle' is a delta, it modifies 'rotationAngle' which then determines 'direction'.
    calcRotation() {
        this.rotationAngle += this.angle; // Apply delta 'angle' to accumulated 'rotationAngle'
        
        // Similar to PlayerCamera, update direction based on accumulated rotationAngle
        this.direction.x = Math.sin(this.rotationAngle);
        this.direction.z = Math.cos(this.rotationAngle);
        this.direction.y = 0; // Assuming 2D rotation for direction
        this.direction.normalize();

        // Update vecside based on new direction and fixed perpendicular (up)
        this.vecside.crossVectors(this.direction, this.perpendicular).normalize();
        
        // directionVel should typically follow direction
        this.directionVel.copy(this.direction);

        this.applyAngFriction(); // Apply friction to the delta 'angle'
    }


    calcVelocity() {
        // Velocity is impulse along directionVel
        this.velocity.copy(this.directionVel).multiplyScalar(this.impulse);
    }

    updatePosition(deltaTime) {
        this.calcVelocity();

        let newpos = this.position.clone().add(this.velocity.clone().multiplyScalar(deltaTime));
        let endCellArray = [null]; // Array to hold the output cell

        if (this.labyrinth) {
            const collided = this.labyrinth.crossLabyrinth(
                this.radius,
                this.position, // current position
                newpos,       // desired new position (will be modified by crossLabyrinth on collision)
                this.normal,  // output normal (will be modified)
                this.collisionPoint, // output collision point (will be modified)
                endCellArray  // output cell (array to pass by reference)
            );

            this.applynormal = collided;
        }
        
        this.position.copy(newpos); // newpos is now the collision-adjusted position

        if (this.applynormal) {
            // Original Java code applies normal impulse here.
            // This means bouncing off the wall.
            // v -= normal * (v.normal()) * (1+bouncefactor)
            // For now, just stop movement into wall. A proper bounce needs bouncefactor.
            // A simple way to stop penetration: project velocity onto plane of collision.
            // Or, more simply, if a collision happened, the crossLabyrinth already adjusted newpos.
            // The impulse might need adjustment based on normal.
            // current_impulse_along_normal = this.velocity.dot(this.normal);
            // this.velocity.addScaledVector(this.normal, -current_impulse_along_normal * (1 + bounceFactor) );
            // For now, let's assume crossLabyrinth handles the position adjustment correctly
            // and we might need to adjust velocity/impulse later if we want bounce.
        }

        this.calcRotation(); // Rotates based on this.angle (delta)
        this.applyFriction(); // Apply friction to this.impulse

        this.m_cell = endCellArray[0] ? endCellArray[0] : this.updateCurrentCell();
    }
}
