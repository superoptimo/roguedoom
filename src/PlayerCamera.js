import * as THREE from 'three';
import { Labyrinth } from './Labyrinth.js';
import { PhysicEntity } from './PhysicEntity.js';

export class PlayerCamera extends PhysicEntity {
    constructor(threeCamera, labyrinth) {
        super(labyrinth); // Call PhysicEntity constructor
        this.threeCamera = threeCamera;

        // Player-specific PhysicEntity properties
        this.radius = 1.0; // Player's collision radius
        this.friction = 0.1; // Player specific friction
        this.angfriction = 0.2; // Player specific angular friction (higher for more responsive turning stop)
        
        this.moveSpeed = 20.0; // Max impulse speed for player
        this.rotationSpeedFactor = 0.1; // Factor to convert key press duration into 'angle' for PhysicEntity

        this.playerHeight = Labyrinth.CELLHEIGHT / 2; // Eye level above the cell's floor y_base_floor_top
        this.isElevating = false; // Flag to indicate if player is currently using an elevator

        // Initial position setup
        if (this.labyrinth) {
            const initialRow = 10;
            const initialCol = 10;
            const initialUpstairs = false;
            const startCell = this.labyrinth.getCell(initialRow, initialCol, initialUpstairs);

            if (startCell) {
                const cellPosData = this.labyrinth.getCellPosition(startCell.m_row, startCell.m_column, startCell.m_upstairs);
                this.position.set(cellPosData.x_center, cellPosData.y_base_floor_top + this.playerHeight, cellPosData.z_center);
                this.m_cell = startCell;
            } else {
                // Fallback if cell not found
                this.position.set(Labyrinth.CELLWIDTH * 10.5, this.playerHeight, Labyrinth.CELLWIDTH * 10.5); // Center of (10,10)
                this.updateCurrentCell(); // Try to find cell from position
            }
        } else {
            this.position.set(0, this.playerHeight, 0);
            this.updateCurrentCell();
        }
        
        // Initial camera sync
        this.threeCamera.position.copy(this.position);
        // Direction is (0,0,1) and rotationAngle is 0 by default from PhysicEntity
        this.updateCameraLookAt(); 

        // Keyboard input state
        this.keys = {
            'ArrowUp': false, 'ArrowDown': false,
            'ArrowLeft': false, 'ArrowRight': false,
            ' ': false, // Space key for firing
        };
        this.onKeyDown = this.onKeyDown.bind(this);
        this.onKeyUp = this.onKeyUp.bind(this);
        window.addEventListener('keydown', this.onKeyDown);
        window.addEventListener('keyup', this.onKeyUp);
    }

    onKeyDown(event) { 
        if (event.key in this.keys) {
            // Prevent continuous firing if space is held down. Only fire on first press.
            if (event.key === ' ' && this.keys[' ']) { 
                return; // Already pressed and registered
            }
            this.keys[event.key] = true;

            if (event.key === ' ' && this.labyrinth) { // Fire on space press
                // Calculate initial rocket position: player's camera position + direction vector * offset
                // Player's base position is this.position. Player's eye level is this.position.y + this.playerHeight
                // Rocket should originate from roughly eye level.
                const rocketInitialPos = new THREE.Vector3(
                    this.position.x + this.direction.x * (this.radius + 0.5), // Offset by player radius + a bit
                    this.position.y + this.playerHeight + this.direction.y * (this.radius + 0.5), // Eye level + offset
                    this.position.z + this.direction.z * (this.radius + 0.5)
                );

                // Rocket's initial direction is player's current direction
                const rocketInitialDir = this.direction.clone();
                
                // console.log("Player Firing Rocket:");
                // console.log(" - Player Position:", this.position);
                // console.log(" - Player Direction:", this.direction);
                // console.log(" - Rocket Initial Position:", rocketInitialPos);
                // console.log(" - Rocket Initial Direction:", rocketInitialDir);

                this.labyrinth.addRocket(rocketInitialDir, rocketInitialPos);
            }
        } 
    }
    onKeyUp(event) { if (event.key in this.keys) this.keys[event.key] = false; }

    updateCameraLookAt() {
        // Ensure Y position of camera is playerHeight above the PhysicEntity's base position's Y
        // PhysicEntity.position.y is its "feet" or base. Camera is at eye level.
        const cameraY = this.position.y + this.playerHeight;
        this.threeCamera.position.set(this.position.x, cameraY, this.position.z);

        const lookAtPoint = new THREE.Vector3().addVectors(this.threeCamera.position, this.direction);
        this.threeCamera.lookAt(lookAtPoint);
    }

    update(deltaTime) {
        if (this.isElevating) {
            // Player movement is controlled by the elevator
            // Friction and direct input impulse/angle should be suppressed or handled differently
            this.impulse = 0;
            this.angle = 0; 
            // PhysicEntity's updatePosition will still apply friction to these zeroed values
            // and handle minimal movement/rotation. Essential for collision response if needed.
            super.updatePosition(deltaTime);

        } else {
            // Normal movement processing
            if (this.keys['ArrowUp']) {
                this.impulse = this.moveSpeed;
            } else if (this.keys['ArrowDown']) {
                this.impulse = -this.moveSpeed / 2;
            } else {
                // Friction in PhysicEntity will handle slowdown if no key pressed
            }

            if (this.keys['ArrowLeft']) {
                this.angle = this.rotationSpeedFactor;
            } else if (this.keys['ArrowRight']) {
                this.angle = -this.rotationSpeedFactor;
            } else {
                // Angular friction in PhysicEntity will handle slowdown
            }
            
            super.updatePosition(deltaTime); // Apply physics and collisions

            // Check for elevator entry AFTER position update and collision response
            if (this.m_cell && this.m_cell.mark === Cell.ELEVATOR && !this.isElevating && this.labyrinth.activeElevatorDetails === null) {
                // Check if player is roughly centered on the elevator platform associated with this cell
                // This requires m_cell.m_elevatorPlatformRef to be set during createElevatorPlatform
                const platformRef = this.m_cell.m_elevatorPlatformRef;
                if (platformRef && platformRef.state === 'idle') {
                    const platformCenterX = platformRef.mesh.position.x;
                    const platformCenterZ = platformRef.mesh.position.z;
                    const distSq = (this.position.x - platformCenterX)**2 + (this.position.z - platformCenterZ)**2;
                    
                    // Check if player is within the 2x2 platform area (approx CELLWIDTH radius from center of 2x2)
                    if (distSq < (Labyrinth.CELLWIDTH * 1.0)**2) { // Player needs to be reasonably centered
                        this.isElevating = true;
                        this.applyfriction = false; // Disable player-controlled friction while elevating
                        this.impulse = 0; // Stop player's own momentum
                        this.angle = 0;   // Stop player's own rotation input

                        const currentPlatformY = platformRef.mesh.position.y;
                        const targetFloorY = this.m_upstairs ? 
                            (this.labyrinth.getCellPosition(platformRef.refCellRow, platformRef.refCellCol, false).y_base_floor_top + this.labyrinth.getPlatformThickness()/2) : 
                            (this.labyrinth.getCellPosition(platformRef.refCellRow, platformRef.refCellCol, true).y_base_floor_top + this.labyrinth.getPlatformThickness()/2) ;

                        platformRef.state = this.m_upstairs ? 'moving_down' : 'moving_up';
                        
                        this.labyrinth.activeElevatorDetails = {
                            platformRef: platformRef,
                            player: this,
                            originalPlayerYOffset: this.position.y - currentPlatformY, // Should be playerHeight - platformThickness/2
                            targetFloorY: targetFloorY 
                        };
                        console.log(`Elevator activated. Player upstairs: ${this.m_upstairs}. Platform state: ${platformRef.state}. TargetY: ${targetFloorY}`);
                    }
                }
            }
        }
        // Update the Three.js camera based on the new state from PhysicEntity (or elevator)
        this.updateCameraLookAt();
    }

    dispose() {
        window.removeEventListener('keydown', this.onKeyDown);
        window.removeEventListener('keyup', this.onKeyUp);
    }
}
