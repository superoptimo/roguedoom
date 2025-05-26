import { Cell } from './Cell.js';
import * as THREE from 'three'; // Make sure THREE is imported for Geometries/Materials
import { LeonMath } from './LeonMath.js'; // Required for crossLabyrinth

export class Labyrinth {
    static MAXELEVATORS = 4;
    static CELLWIDTH = 10; 
    static CELLHEIGHT = 10; 
    static WALLTHICK = 1;   

    constructor(scene) {
        this.scene = scene; 
        this.textureLoader = new THREE.TextureLoader();
        this.createTexturedMaterials(); 
        this.cells = new Array(800); 
        this.elevator_col = new Array(Labyrinth.MAXELEVATORS);
        this.elevator_row = new Array(Labyrinth.MAXELEVATORS);
        this.elevatorCount = 0;
        
        this.elevatorPlatforms = []; 
        this.activeElevatorDetails = null; 
        this.activeRockets = []; // Array to hold active rockets
        
        this.generateLabyrinth(); 
        this.create3DRepresentation(); 
    }

    addRocket(initialDirection, initialPosition) {
        // LeonRocket needs to be imported in Labyrinth.js
        const rocket = new LeonRocket(this, initialDirection, initialPosition); // 'this' (Labyrinth instance) is passed
        this.activeRockets.push(rocket);
        // console.log(`Added rocket. Total active rockets: ${this.activeRockets.length}`);
    }

    updateRockets(deltaTime) {
        for (let i = this.activeRockets.length - 1; i >= 0; i--) {
            const rocket = this.activeRockets[i];
            rocket.update(deltaTime);
            if (!rocket.active) {
                this.activeRockets.splice(i, 1); // Remove inactive rocket
                // console.log(`Removed rocket. Total active rockets: ${this.activeRockets.length}`);
            }
        }
    }

    getPlatformThickness() { return 0.5; } 

    updateElevators(deltaTime, playerEntity) { 
        if (this.activeElevatorDetails) {
            const { platformRef, player, originalPlayerYOffset, targetFloorY } = this.activeElevatorDetails;
            const platformMesh = platformRef.mesh;
            // Ensure speed is reasonable. CELLHEIGHT is 10. Moving one level in 1 second.
            const speed = Labyrinth.CELLHEIGHT / 1.0; 

            let currentY = platformMesh.position.y;
            let newPlatformY;

            if (platformRef.state === 'moving_up') {
                newPlatformY = Math.min(currentY + speed * deltaTime, targetFloorY);
            } else if (platformRef.state === 'moving_down') {
                newPlatformY = Math.max(currentY - speed * deltaTime, targetFloorY);
            } else {
                return; 
            }

            const dy = newPlatformY - currentY; 
            platformMesh.position.y = newPlatformY;
            platformRef.currentY = newPlatformY; // Update the stored Y in platformData
            
            if (player) {
                player.position.y += dy; 
                if(player.threeCamera) { // PlayerCamera has threeCamera
                   player.threeCamera.position.y = player.position.y + player.playerHeight; 
                }
            }

            if (Math.abs(newPlatformY - targetFloorY) < 0.05) { 
                platformMesh.position.y = targetFloorY; 
                platformRef.currentY = targetFloorY;
                
                if (player) {
                    // Player's base should be on the surface of the platform.
                    // Platform center is at targetFloorY. Player base is targetFloorY + platformThickness/2.
                    player.position.y = targetFloorY + this.getPlatformThickness() / 2;
                     if(player.threeCamera) {
                        player.threeCamera.position.y = player.position.y + player.playerHeight;
                    }

                    player.m_upstairs = targetFloorY > (Labyrinth.CELLHEIGHT + this.getPlatformThickness()*2); // Heuristic: if platform Y is > one level height
                    const newPlayerCell = this.getCellFromPosition(player.position);
                    player.m_cell = newPlayerCell;
                    
                    player.isElevating = false; 
                    player.applyfriction = true; 
                    console.log("Elevator reached. Player cell:", newPlayerCell ? `${newPlayerCell.m_row},${newPlayerCell.m_column} mark: ${newPlayerCell.mark}` : "None", "Upstairs:", player.m_upstairs);
                }
                
                platformRef.state = 'idle';
                this.activeElevatorDetails = null;
            }
        }
    }

    createTexturedMaterials() {
        const basePath = 'assets/images/';
        this.texRedFloor = this.textureLoader.load(basePath + 'REDFLOOR.GIF');
        this.texRedCeil = this.textureLoader.load(basePath + 'REDCEIL.GIF');
        this.texRedWall = this.textureLoader.load(basePath + 'REDWALL.GIF');
        this.texRedBaseWall = this.textureLoader.load(basePath + 'REDBASEW.GIF');
        this.texBlueFloor = this.textureLoader.load(basePath + 'BLUEFLOOR.GIF');
        this.texBlueCeil = this.textureLoader.load(basePath + 'BLUECEIL.GIF');
        this.texBlueWall = this.textureLoader.load(basePath + 'BLUEWALL.GIF');
        this.texBlueBaseWall = this.textureLoader.load(basePath + 'BLUEBASEW.GIF');
        this.texElevatorWall = this.textureLoader.load(basePath + 'ELEVATOR.GIF'); 
        this.texElevatorPlatform = this.textureLoader.load(basePath + 'ELEVATOR.GIF'); 
        this.texElevatorFloor = this.textureLoader.load(basePath + 'ELEVATORFL.GIF'); 
        this.texRockWall = this.textureLoader.load(basePath + 'ROCKWALL.GIF'); 
        this.texRedAltar = this.textureLoader.load(basePath + 'REDALTAR.GIF');
        this.texBlueAltar = this.textureLoader.load(basePath + 'BLUEALTAR.GIF');
        this.texRedSource = this.textureLoader.load(basePath + 'REDSOURCE.GIF');
        this.texBlueSource = this.textureLoader.load(basePath + 'BLUESOURCE.GIF');

        this.matRedFloor = new THREE.MeshStandardMaterial({ map: this.texRedFloor });
        this.matRedCeil = new THREE.MeshStandardMaterial({ map: this.texRedCeil });
        this.matRedWall = new THREE.MeshStandardMaterial({ map: this.texRedWall });
        this.matRedBaseWall = new THREE.MeshStandardMaterial({ map: this.texRedBaseWall });
        this.matBlueFloor = new THREE.MeshStandardMaterial({ map: this.texBlueFloor });
        this.matBlueCeil = new THREE.MeshStandardMaterial({ map: this.texBlueCeil });
        this.matBlueWall = new THREE.MeshStandardMaterial({ map: this.texBlueWall });
        this.matBlueBaseWall = new THREE.MeshStandardMaterial({ map: this.texBlueBaseWall });
        this.matElevatorWall = new THREE.MeshStandardMaterial({ map: this.texElevatorWall }); 
        this.matElevatorPlatform = new THREE.MeshStandardMaterial({ 
            map: this.texElevatorPlatform, transparent: true, opacity: 0.75, side: THREE.DoubleSide });
        this.matElevatorFloor = new THREE.MeshStandardMaterial({ map: this.texElevatorFloor }); 
        this.matRockWall = new THREE.MeshStandardMaterial({ map: this.texRockWall });
        this.matRedAltar = new THREE.MeshStandardMaterial({ map: this.texRedAltar, side: THREE.DoubleSide });
        this.matBlueAltar = new THREE.MeshStandardMaterial({ map: this.texBlueAltar, side: THREE.DoubleSide });
        this.matRedSource = new THREE.MeshStandardMaterial({ map: this.texRedSource, side: THREE.DoubleSide });
        this.matBlueSource = new THREE.MeshStandardMaterial({ map: this.texBlueSource, side: THREE.DoubleSide });
        
        this.matDefaultFloor = new THREE.MeshStandardMaterial({ color: 0x777777, side: THREE.DoubleSide });
        this.matDefaultCeil = new THREE.MeshStandardMaterial({ color: 0x999999, side: THREE.DoubleSide });
        this.matDefaultWall = new THREE.MeshStandardMaterial({ color: 0x555555 });

        const texturesToRepeat = [
            this.texRedFloor, this.texRedCeil, this.texRedWall, this.texRedBaseWall,
            this.texBlueFloor, this.texBlueCeil, this.texBlueWall, this.texBlueBaseWall,
            this.texElevatorWall, this.texElevatorPlatform, this.texElevatorFloor, 
            this.texRockWall,
            this.texRedAltar, this.texBlueAltar, this.texRedSource, this.texBlueSource];
        for (const texture of texturesToRepeat) {
            if (texture) { texture.wrapS = THREE.RepeatWrapping; texture.wrapT = THREE.RepeatWrapping; }
        }
    }

    static NORTH = 0; static EAST = 1; static SOUTH = 2; static WEST = 3;

    getCellNeighbor(cell, dir) { /* ... (implementation from previous turns) ... */ 
        if (!cell) return null;
        let { m_row: row, m_column: col, m_upstairs: upstairs } = cell;
        switch (dir) {
            case Labyrinth.NORTH: row--; break;
            case Labyrinth.EAST:  col++; break;
            case Labyrinth.SOUTH: row++; break;
            case Labyrinth.WEST:  col--; break;
        }
        return this.getCell(row, col, upstairs);
    }
    getCellBound(cell, dir) { /* ... (implementation from previous turns) ... */ 
        if (!cell) return true; 
        switch (dir) {
            case Labyrinth.NORTH:
                if (cell.m_row === 0) return true;
                const neighborN = this.getCellNeighbor(cell, Labyrinth.NORTH);
                return neighborN ? neighborN.SouthBound : true; 
            case Labyrinth.EAST: return cell.EastBound;
            case Labyrinth.SOUTH: return cell.SouthBound;
            case Labyrinth.WEST:
                if (cell.m_column === 0) return true;
                const neighborW = this.getCellNeighbor(cell, Labyrinth.WEST);
                return neighborW ? neighborW.EastBound : true; 
        }
        return true;
    }
    getOppositeWall(dir) { /* ... (implementation from previous turns) ... */ 
        switch (dir) {
            case Labyrinth.NORTH: return Labyrinth.SOUTH;
            case Labyrinth.EAST:  return Labyrinth.WEST;
            case Labyrinth.SOUTH: return Labyrinth.NORTH;
            case Labyrinth.WEST:  return Labyrinth.EAST;
        }
        return -1; 
    }
    getCellNormal(dir, outNormal) { /* ... (implementation from previous turns) ... */ 
        switch (dir) {
            case Labyrinth.NORTH: outNormal.set(0, 0, 1);  break; 
            case Labyrinth.EAST:  outNormal.set(-1, 0, 0); break; 
            case Labyrinth.SOUTH: outNormal.set(0, 0, -1); break; 
            case Labyrinth.WEST:  outNormal.set(1, 0, 0);  break; 
            default: outNormal.set(0,0,0); return false; 
        }
        return true;
    }
    getWallPlaneD(cell, dir) { /* ... (implementation from previous turns) ... */ 
        const cellMinX = cell.m_column * Labyrinth.CELLWIDTH;
        const cellMinZ = cell.m_row * Labyrinth.CELLWIDTH;
        switch (dir) {
            case Labyrinth.NORTH: return cellMinZ; 
            case Labyrinth.EAST:  return cellMinX + Labyrinth.CELLWIDTH; 
            case Labyrinth.SOUTH: return cellMinZ + Labyrinth.CELLWIDTH; 
            case Labyrinth.WEST:  return cellMinX; 
        }
        return 0;
    }
    isSolidCorner(cell, corner_index) { /* ... (implementation from previous turns) ... */ 
        if (!cell) return true; 
        const { m_row: r, m_column: c, m_upstairs: u } = cell;
        let wall1_dir, wall2_dir;
        switch (corner_index) {
            case 0: wall1_dir = Labyrinth.NORTH; wall2_dir = Labyrinth.WEST; break;
            case 1: wall1_dir = Labyrinth.NORTH; wall2_dir = Labyrinth.EAST; break;
            case 2: wall1_dir = Labyrinth.SOUTH; wall2_dir = Labyrinth.EAST; break;
            case 3: wall1_dir = Labyrinth.SOUTH; wall2_dir = Labyrinth.WEST; break;
            default: return true;
        }
        const bound1 = this.getCellBound(cell, wall1_dir);
        const bound2 = this.getCellBound(cell, wall2_dir);
        return bound1 && bound2;
    }

    crossLabyrinth(radius, center, endposition, normal, collisionpoint, endcell_arr) { /* ... (implementation from previous turns, ensure LeonMath is imported and THREE is available) ... */ 
        let collision = false;
        let total_dist_sq = center.distanceToSquared(endposition);
        if (total_dist_sq < 0.000001) { 
            endcell_arr[0] = this.getCellFromPosition(center);
            return false;
        }
        let vel = new THREE.Vector3().subVectors(endposition, center);
        let best_t = 1.0; 
        let current_cell = this.getCellFromPosition(center);
        endcell_arr[0] = current_cell;
        if (!current_cell) return false; 
        const stack = [current_cell];
        const visited = new Set(); 
        visited.add(this.getCellIndex(current_cell));
        const tempNormal = new THREE.Vector3();
        const tempCollisionPoint = new THREE.Vector3();
        const timeScalar = [Infinity]; 
        let safetyIteration = 0;
        while (stack.length > 0 && safetyIteration < 50) { 
            safetyIteration++;
            current_cell = stack.pop();
            if (!current_cell) continue;
            for (let dir = 0; dir < 4; dir++) { 
                if (this.getCellBound(current_cell, dir)) {
                    this.getCellNormal(dir, tempNormal); 
                    let planeD;
                    const cellMinX = current_cell.m_column * Labyrinth.CELLWIDTH;
                    const cellMinZ = current_cell.m_row * Labyrinth.CELLWIDTH;
                    switch (dir) {
                        case Labyrinth.NORTH: planeD = cellMinZ; break;
                        case Labyrinth.EAST:  planeD = cellMinX + Labyrinth.CELLWIDTH; break;
                        case Labyrinth.SOUTH: planeD = cellMinZ + Labyrinth.CELLWIDTH; break;
                        case Labyrinth.WEST:  planeD = cellMinX; break;
                    }
                    let checkVel = vel.clone().multiplyScalar(best_t); 
                    if (LeonMath.collisionSphereVelPlane(radius, center, checkVel, tempNormal, planeD, timeScalar, tempCollisionPoint)) {
                        if (timeScalar[0] < 1.0) { 
                            collision = true;
                            best_t *= timeScalar[0]; 
                            normal.copy(tempNormal);
                            collisionpoint.copy(tempCollisionPoint);
                        }
                    }
                }
            } 
            const corners = [ 
                new THREE.Vector3(current_cell.m_column * Labyrinth.CELLWIDTH, center.y, current_cell.m_row * Labyrinth.CELLWIDTH), 
                new THREE.Vector3((current_cell.m_column + 1) * Labyrinth.CELLWIDTH, center.y, current_cell.m_row * Labyrinth.CELLWIDTH), 
                new THREE.Vector3((current_cell.m_column + 1) * Labyrinth.CELLWIDTH, center.y, (current_cell.m_row + 1) * Labyrinth.CELLWIDTH), 
                new THREE.Vector3(current_cell.m_column * Labyrinth.CELLWIDTH, center.y, (current_cell.m_row + 1) * Labyrinth.CELLWIDTH)  
            ];
            for (let i = 0; i < 4; i++) {
                if (this.isSolidCorner(current_cell, i)) {
                    let checkVel = vel.clone().multiplyScalar(best_t);
                    if (LeonMath.collisionSphereVelSphere(radius, center, checkVel, 0.001, corners[i], timeScalar)) { 
                        if (timeScalar[0] < 1.0) {
                            collision = true;
                            best_t *= timeScalar[0];
                            let collisionCenterTime = center.clone().addScaledVector(checkVel, timeScalar[0]);
                            normal.subVectors(collisionCenterTime, corners[i]).normalize();
                            collisionpoint.copy(corners[i]); 
                        }
                    }
                }
            } 
            if (!collision || best_t > 0.999) { 
                for (let dir = 0; dir < 4; dir++) {
                    if (!this.getCellBound(current_cell, dir)) {
                        const neighbor = this.getCellNeighbor(current_cell, dir);
                        if (neighbor && !visited.has(this.getCellIndex(neighbor))) {
                            let points_towards = false;
                            if (dir === Labyrinth.NORTH && vel.z < 0) points_towards = true;
                            if (dir === Labyrinth.EAST && vel.x > 0) points_towards = true;
                            if (dir === Labyrinth.SOUTH && vel.z > 0) points_towards = true;
                            if (dir === Labyrinth.WEST && vel.x < 0) points_towards = true;
                            if(points_towards){
                                stack.push(neighbor);
                                visited.add(this.getCellIndex(neighbor));
                            }
                        }
                    }
                }
            }
        } 
        endposition.copy(center).addScaledVector(vel, best_t);
        endcell_arr[0] = this.getCellFromPosition(endposition); 
        return collision;
    }

    getCellFromPosition(posVec) { /* ... (implementation from previous turns) ... */ 
        const col = Math.floor(posVec.x / Labyrinth.CELLWIDTH);
        const row = Math.floor(posVec.z / Labyrinth.CELLWIDTH);
        const floorThickness = 0.2; 
        const ceilingThickness = 0.2;
        const singleLevelCellSpace = Labyrinth.CELLHEIGHT;
        const y_level_separator = floorThickness + singleLevelCellSpace + ceilingThickness + floorThickness / 2; // Midpoint between top of lower ceiling and bottom of upper floor
        const upstairs = posVec.y > y_level_separator;
        return this.getCell(row, col, upstairs);
    }    
    getCellIndex(cell) { /* ... (implementation from previous turns) ... */ 
        if (!cell) return -1;
        let num = cell.m_row * 20 + cell.m_column;
        if (cell.m_upstairs) num += 400;
        return num;
    }
    getCellPosition(row, column, upstairs) { /* ... (implementation from previous turns, ensure y_base is y_base_floor_top) ... */ 
        const x_center = column * Labyrinth.CELLWIDTH + Labyrinth.CELLWIDTH / 2;
        const z_center = row * Labyrinth.CELLWIDTH + Labyrinth.CELLWIDTH / 2;
        const floorThickness = 0.2; 
        const ceilingThickness = 0.2; 
        const singleLevelTotalHeight = Labyrinth.CELLHEIGHT + floorThickness + ceilingThickness;
        const y_base_floor_top = upstairs ? singleLevelTotalHeight + floorThickness : floorThickness; 
        return { x_center, y_base_floor_top: y_base_floor_top, z_center }; // Ensure y_base_floor_top is returned
    }

    create3DRepresentation() {
        const floorThickness = 0.2; 
        for (let i = 0; i < 800; i++) {
            const cell = this.cells[i];
            if (!cell) continue;
            switch (cell.mark) {
                case Cell.REDALTAR: case Cell.BLUEALTAR: this.createAltar(cell); break;
                case Cell.REDSOURCE: case Cell.BLUESOURCE: this.createSource(cell); break;
                case Cell.ELEVATOR:
                    this.createElevatorPlatform(cell); 
                    const isBottomElevatorCell = !cell.m_upstairs; 
                    if (isBottomElevatorCell) { 
                        const { x_center, y_base_floor_top, z_center } = this.getCellPosition(cell.m_row, cell.m_column, false);
                        const floorGeo = new THREE.BoxGeometry(Labyrinth.CELLWIDTH, floorThickness, Labyrinth.CELLWIDTH);
                        const floorMesh = new THREE.Mesh(floorGeo, this.matElevatorFloor.map ? this.matElevatorFloor : this.matDefaultFloor);
                        floorMesh.position.set(x_center, y_base_floor_top - floorThickness / 2, z_center);
                        this.scene.add(floorMesh); cell.m_entities.push(floorMesh);
                    }
                    this.createGenericWalls(cell, this.matElevatorWall.map ? this.matElevatorWall : this.matDefaultWall); 
                    break;
                case Cell.ELEVATORROOM: this.createElevatorRoomCell(cell); break;
                default: this.createGenericCell(cell); break;
            }
        }
    }

    createGenericCell(cell) {
        const { x_center, y_base_floor_top, z_center } = this.getCellPosition(cell.m_row, cell.m_column, cell.m_upstairs);
        const actualCellHeight = Labyrinth.CELLHEIGHT;
        const floorThickness = 0.2; const ceilingThickness = 0.2;
        let floorMaterial, ceilMaterial, wallMaterial; 
        if (cell.m_upstairs) {
            floorMaterial = this.matBlueFloor; ceilMaterial = this.matBlueCeil;
            wallMaterial = (cell.mark === Cell.BLUEBASE) ? this.matBlueBaseWall : this.matBlueWall;
        } else {
            floorMaterial = this.matRedFloor; ceilMaterial = this.matRedCeil;
            wallMaterial = (cell.mark === Cell.REDBASE) ? this.matRedBaseWall : this.matRedWall;
        }
        floorMaterial = floorMaterial.map ? floorMaterial : this.matDefaultFloor;
        ceilMaterial = ceilMaterial.map ? ceilMaterial : this.matDefaultCeil;
        wallMaterial = wallMaterial.map ? wallMaterial : this.matDefaultWall;
        const floorGeo = new THREE.BoxGeometry(Labyrinth.CELLWIDTH, floorThickness, Labyrinth.CELLWIDTH);
        const floorMesh = new THREE.Mesh(floorGeo, floorMaterial);
        floorMesh.position.set(x_center, y_base_floor_top - floorThickness / 2, z_center);
        this.scene.add(floorMesh); cell.m_entities.push(floorMesh);
        const ceilGeo = new THREE.BoxGeometry(Labyrinth.CELLWIDTH, ceilingThickness, Labyrinth.CELLWIDTH);
        const ceilMesh = new THREE.Mesh(ceilGeo, ceilMaterial);
        ceilMesh.position.set(x_center, y_base_floor_top + actualCellHeight + ceilingThickness / 2, z_center);
        this.scene.add(ceilMesh); cell.m_entities.push(ceilMesh);
        this.createGenericWalls(cell, wallMaterial);
    }
    
    createGenericWalls(cell, specificWallMaterial) {
        const { x_center, y_base_floor_top, z_center } = this.getCellPosition(cell.m_row, cell.m_column, cell.m_upstairs);
        const actualCellHeight = Labyrinth.CELLHEIGHT;
        const wall_y_center = y_base_floor_top + actualCellHeight / 2;
        let cellWallMaterial = specificWallMaterial;
        if (!cellWallMaterial || !cellWallMaterial.map) { 
             cellWallMaterial = cell.m_upstairs ? (this.matBlueWall.map?this.matBlueWall:this.matDefaultWall) : (this.matRedWall.map?this.matRedWall:this.matDefaultWall);
        }
        if (cell.EastBound) {
            const wg = new THREE.BoxGeometry(Labyrinth.WALLTHICK, actualCellHeight, Labyrinth.CELLWIDTH);
            const wm = new THREE.Mesh(wg, cellWallMaterial);
            wm.position.set(x_center + Labyrinth.CELLWIDTH/2 - Labyrinth.WALLTHICK/2, wall_y_center, z_center);
            this.scene.add(wm); cell.m_entities.push(wm);
        } else if (cell.m_column === 19) { 
            const wg = new THREE.BoxGeometry(Labyrinth.WALLTHICK, actualCellHeight, Labyrinth.CELLWIDTH);
            const wm = new THREE.Mesh(wg, this.matRockWall);
            wm.position.set(x_center + Labyrinth.CELLWIDTH/2 - Labyrinth.WALLTHICK/2, wall_y_center, z_center);
            this.scene.add(wm); cell.m_entities.push(wm);
        }
        if (cell.SouthBound) {
            const wg = new THREE.BoxGeometry(Labyrinth.CELLWIDTH, actualCellHeight, Labyrinth.WALLTHICK);
            const wm = new THREE.Mesh(wg, cellWallMaterial);
            wm.position.set(x_center, wall_y_center, z_center + Labyrinth.CELLWIDTH/2 - Labyrinth.WALLTHICK/2);
            this.scene.add(wm); cell.m_entities.push(wm);
        } else if (cell.m_row === 19) { 
            const wg = new THREE.BoxGeometry(Labyrinth.CELLWIDTH, actualCellHeight, Labyrinth.WALLTHICK);
            const wm = new THREE.Mesh(wg, this.matRockWall);
            wm.position.set(x_center, wall_y_center, z_center + Labyrinth.CELLWIDTH/2 - Labyrinth.WALLTHICK/2);
            this.scene.add(wm); cell.m_entities.push(wm);
        }
        if (cell.m_row === 0) {
            const wg = new THREE.BoxGeometry(Labyrinth.CELLWIDTH, actualCellHeight, Labyrinth.WALLTHICK);
            const wm = new THREE.Mesh(wg, this.matRockWall);
            wm.position.set(x_center, wall_y_center, z_center - Labyrinth.CELLWIDTH/2 + Labyrinth.WALLTHICK/2);
            this.scene.add(wm); cell.m_entities.push(wm);
        }
        if (cell.m_column === 0) {
            const wg = new THREE.BoxGeometry(Labyrinth.WALLTHICK, actualCellHeight, Labyrinth.CELLWIDTH);
            const wm = new THREE.Mesh(wg, this.matRockWall);
            wm.position.set(x_center - Labyrinth.CELLWIDTH/2 + Labyrinth.WALLTHICK/2, wall_y_center, z_center);
            this.scene.add(wm); cell.m_entities.push(wm);
        }
    }

    createAltar(cell) {
        const { x_center, y_base_floor_top, z_center } = this.getCellPosition(cell.m_row, cell.m_column, cell.m_upstairs);
        const actualCellHeight = Labyrinth.CELLHEIGHT; const floorThickness = 0.2; const ceilingThickness = 0.2;
        const altarBaseMat = cell.mark === Cell.REDALTAR ? this.matRedFloor : this.matBlueFloor;
        const altarTierMat = cell.mark === Cell.REDALTAR ? this.matRedAltar : this.matBlueAltar;
        const altarCeilMat = cell.mark === Cell.REDALTAR ? this.matRedCeil : this.matBlueCeil;
        const floor = new THREE.Mesh(new THREE.BoxGeometry(Labyrinth.CELLWIDTH, floorThickness, Labyrinth.CELLWIDTH), altarBaseMat.map?altarBaseMat:this.matDefaultFloor);
        floor.position.set(x_center, y_base_floor_top - floorThickness/2, z_center); this.scene.add(floor); cell.m_entities.push(floor);
        const ceil = new THREE.Mesh(new THREE.BoxGeometry(Labyrinth.CELLWIDTH, ceilingThickness, Labyrinth.CELLWIDTH), altarCeilMat.map?altarCeilMat:this.matDefaultCeil);
        ceil.position.set(x_center, y_base_floor_top + actualCellHeight + ceilingThickness/2, z_center); this.scene.add(ceil); cell.m_entities.push(ceil);
        const tierRadii = [2.5,2.0,1.5]; const tierHeight = 0.2; let currentTierY = y_base_floor_top + tierHeight/2;
        for (let i=0; i<tierRadii.length; i++) {
            const tier = new THREE.Mesh(new THREE.CylinderGeometry(tierRadii[i],tierRadii[i],tierHeight,32), altarTierMat.map?altarTierMat:this.matDefaultWall);
            tier.position.set(x_center, currentTierY, z_center); this.scene.add(tier); cell.m_entities.push(tier); currentTierY += tierHeight;
        }
        this.createGenericWalls(cell, altarTierMat.map?altarTierMat:(cell.m_upstairs?this.matBlueWall:this.matRedWall));
    }

    createSource(cell) {
        const {x_center,y_base_floor_top,z_center} = this.getCellPosition(cell.m_row,cell.m_column,cell.m_upstairs);
        const actualCellHeight = Labyrinth.CELLHEIGHT; const floorThickness=0.2; const ceilingThickness=0.2;
        const sourceFloorMat = cell.m_upstairs ? (this.matBlueFloor.map?this.matBlueFloor:this.matDefaultFloor) : (this.matRedFloor.map?this.matRedFloor:this.matDefaultFloor);
        const sourceCeilMat = cell.m_upstairs ? (this.matBlueCeil.map?this.matBlueCeil:this.matDefaultCeil) : (this.matRedCeil.map?this.matRedCeil:this.matDefaultCeil);
        const sourceStructMat = cell.mark===Cell.REDSOURCE ? (this.matRedSource.map?this.matRedSource:this.matDefaultWall) : (this.matBlueSource.map?this.matBlueSource:this.matDefaultWall);
        const sourceSideWallMat = cell.mark===Cell.REDSOURCE ? (this.matRedBaseWall.map?this.matRedBaseWall:this.matDefaultWall) : (this.matBlueBaseWall.map?this.matBlueBaseWall:this.matDefaultWall);
        const teleporterMat = this.matElevatorPlatform.map?this.matElevatorPlatform:this.matDefaultWall;
        const floor = new THREE.Mesh(new THREE.BoxGeometry(Labyrinth.CELLWIDTH,floorThickness,Labyrinth.CELLWIDTH),sourceFloorMat);
        floor.position.set(x_center,y_base_floor_top-floorThickness/2,z_center); this.scene.add(floor); cell.m_entities.push(floor);
        const ceil = new THREE.Mesh(new THREE.BoxGeometry(Labyrinth.CELLWIDTH,ceilingThickness,Labyrinth.CELLWIDTH),sourceCeilMat);
        ceil.position.set(x_center,y_base_floor_top+actualCellHeight+ceilingThickness/2,z_center); this.scene.add(ceil); cell.m_entities.push(ceil);
        const platH=0.2; const platW=2.5*2;
        const botPlat = new THREE.Mesh(new THREE.BoxGeometry(platW,platH,platW),sourceStructMat);
        botPlat.position.set(x_center,y_base_floor_top+platH/2,z_center); this.scene.add(botPlat); cell.m_entities.push(botPlat);
        const topPlat = new THREE.Mesh(new THREE.BoxGeometry(platW,platH,platW),sourceStructMat);
        topPlat.position.set(x_center,y_base_floor_top+actualCellHeight-platH/2,z_center); this.scene.add(topPlat); cell.m_entities.push(topPlat);
        const sideWallH=actualCellHeight-2*platH; const sideWallW=0.2*2; const sideWallD=platW;
        const sideY=y_base_floor_top+platH+sideWallH/2;
        const wallG=new THREE.BoxGeometry(sideWallW,sideWallH,sideWallD);
        const wall1=new THREE.Mesh(wallG,sourceSideWallMat); wall1.position.set(x_center-platW/2+sideWallW/2,sideY,z_center); this.scene.add(wall1); cell.m_entities.push(wall1);
        const wall2=new THREE.Mesh(wallG,sourceSideWallMat); wall2.position.set(x_center+platW/2-sideWallW/2,sideY,z_center); this.scene.add(wall2); cell.m_entities.push(wall2);
        const teleRadius=1.0; const teleH=actualCellHeight-platH;
        const teleGeo=new THREE.CylinderGeometry(teleRadius,teleRadius,teleH,32);
        const teleMesh=new THREE.Mesh(teleGeo,teleporterMat);
        teleMesh.position.set(x_center,y_base_floor_top+platH/2+teleH/2,z_center); this.scene.add(teleMesh); cell.m_entities.push(teleMesh);
        this.createGenericWalls(cell, sourceStructMat);
    }

    createElevatorPlatform(cell) {
        const platformThickness = this.getPlatformThickness(); 
        const platformWidth = Labyrinth.CELLWIDTH * 2; const platformDepth = Labyrinth.CELLWIDTH * 2;
        const platformGeo = new THREE.BoxGeometry(platformWidth,platformThickness,platformDepth);
        const platformMat = this.matElevatorPlatform.map?this.matElevatorPlatform:this.matDefaultFloor;
        const platformMesh = new THREE.Mesh(platformGeo,platformMat);
        let baseElevatorInfo = null;
        for(let i=0; i<this.elevatorCount; i++) {
            const e_c = this.elevator_col[i]; const e_r = this.elevator_row[i];
            if (cell.m_column >= e_c+1 && cell.m_column < e_c+3 && cell.m_row >= e_r+1 && cell.m_row < e_r+3) {
                baseElevatorInfo = { base_col:e_c+1, base_row:e_r+1 }; break;
            }
        }
        if (!baseElevatorInfo) { console.error("Elevator cell not in defined structure",cell); return; }
        const refCellCol=baseElevatorInfo.base_col; const refCellRow=baseElevatorInfo.base_row;
        let existingPlatform = this.elevatorPlatforms.find(p=>p.refCellCol===refCellCol && p.refCellRow===refCellRow);
        if(!existingPlatform){
            const platform_x = (refCellCol+1)*Labyrinth.CELLWIDTH; const platform_z = (refCellRow+1)*Labyrinth.CELLWIDTH;
            const { y_base_floor_top } = this.getCellPosition(cell.m_row, cell.m_column, cell.m_upstairs);
            const initial_y = y_base_floor_top + platformThickness/2;
            platformMesh.position.set(platform_x, initial_y, platform_z);
            this.scene.add(platformMesh);
            const platformData = {mesh:platformMesh, refCellCol:refCellCol, refCellRow:refCellRow, currentY:initial_y, state:'idle'};
            this.elevatorPlatforms.push(platformData);
            for(let r_off=0;r_off<2;r_off++){ for(let c_off=0;c_off<2;c_off++){
                const sCellDown = this.getCell(refCellRow+r_off, refCellCol+c_off, false);
                if(sCellDown && sCellDown.mark === Cell.ELEVATOR) sCellDown.m_elevatorPlatformRef = platformData;
                const sCellUp = this.getCell(refCellRow+r_off, refCellCol+c_off, true);
                if(sCellUp && sCellUp.mark === Cell.ELEVATOR) sCellUp.m_elevatorPlatformRef = platformData;
            }}
        }
    }

    createElevatorRoomCell(cell) {
        const {x_center,y_base_floor_top,z_center} = this.getCellPosition(cell.m_row,cell.m_column,cell.m_upstairs);
        const actualCellHeight = Labyrinth.CELLHEIGHT; const floorThickness=0.2; const ceilingThickness=0.2;
        let hasFloor=false; let hasCeiling=false;
        if(!cell.m_upstairs){ // Downstairs cell
            const cellAbove = this.getCell(cell.m_row,cell.m_column,true);
            // Add floor if it's truly the bottom of the shaft (nothing relevant above, or above is part of elevator)
            if(!cellAbove || (cellAbove.mark === Cell.ELEVATOR || cellAbove.mark === Cell.ELEVATORROOM)){
                 hasFloor = true; 
            }
            // No ceiling if cell above is part of elevator shaft. Otherwise, it's a single-story room, needs ceiling.
            if(cellAbove && cellAbove.mark !== Cell.ELEVATOR && cellAbove.mark !== Cell.ELEVATORROOM) {
                hasCeiling = true;
            }
        } else { // Upstairs cell
            const cellBelow = this.getCell(cell.m_row,cell.m_column,false);
            // Needs a floor if cell below is not part of elevator shaft
            if(!cellBelow || (cellBelow.mark !== Cell.ELEVATOR && cellBelow.mark !== Cell.ELEVATORROOM)){
                hasFloor = true;
            }
            hasCeiling = true; // All upstairs rooms generally have ceilings
        }

        if(hasFloor){
            const floorG = new THREE.BoxGeometry(Labyrinth.CELLWIDTH,floorThickness,Labyrinth.CELLWIDTH);
            const floorM = new THREE.Mesh(floorG, this.matElevatorFloor.map?this.matElevatorFloor:this.matDefaultFloor);
            floorM.position.set(x_center,y_base_floor_top-floorThickness/2,z_center); this.scene.add(floorM); cell.m_entities.push(floorM);
        }
        if(hasCeiling){
            const ceilG = new THREE.BoxGeometry(Labyrinth.CELLWIDTH,ceilingThickness,Labyrinth.CELLWIDTH);
            const ceilM = new THREE.Mesh(ceilG, this.matElevatorFloor.map?this.matElevatorFloor:this.matDefaultCeil);
            ceilM.position.set(x_center,y_base_floor_top+actualCellHeight+ceilingThickness/2,z_center); this.scene.add(ceilM); cell.m_entities.push(ceilM);
        }
        this.createGenericWalls(cell, this.matElevatorWall.map?this.matElevatorWall:this.matDefaultWall);
    }

    getCell(row, column, upstairs) {
        if (row < 0 || row >= 20 || column < 0 || column >= 20) return null;
        let num = row * 20 + column + (upstairs ? 400 : 0);
        return (num >= 0 && num < 800) ? this.cells[num] : null;
    }
    getUpstairsCell(row, column) { return this.getCell(row, column, true); }
    getDownstairsCell(row, column) { return this.getCell(row, column, false); }

    generateLabyrinth() {
        for (let i = 0; i < 800; i++) {
            const upstairs = i >= 400; const baseIndex = upstairs ? i-400 : i;
            this.cells[i] = new Cell(Math.floor(baseIndex/20), baseIndex%20, upstairs);
        }
        const rb_r = Math.floor(Math.random()*17); const rb_c = Math.floor(Math.random()*17);
        for(let r=0;r<4;r++)for(let c=0;c<4;c++){ const cl=this.getDownstairsCell(rb_r+r,rb_c+c); if(cl){cl.mark=Cell.REDBASE; if(r===3)cl.SouthBound=true; if(c===3)cl.EastBound=true;}}
        for(let i=0;i<4;i++){ let cl=this.getDownstairsCell(rb_r+i,rb_c-1); if(cl)cl.EastBound=true; cl=this.getDownstairsCell(rb_r-1,rb_c+i); if(cl)cl.SouthBound=true;}
        const bb_r = Math.floor(Math.random()*17); const bb_c = Math.floor(Math.random()*17);
        for(let r=0;r<4;r++)for(let c=0;c<4;c++){ const cl=this.getUpstairsCell(bb_r+r,bb_c+c); if(cl){cl.mark=Cell.BLUEBASE; if(r===3)cl.SouthBound=true; if(c===3)cl.EastBound=true;}}
        for(let i=0;i<4;i++){ let cl=this.getUpstairsCell(bb_r+i,bb_c-1); if(cl)cl.EastBound=true; cl=this.getUpstairsCell(bb_r-1,bb_c+i); if(cl)cl.SouthBound=true;}
        this.elevatorCount=0;
        for(let i=0;i<Labyrinth.MAXELEVATORS;i++){
            let e_r,e_c,valid=false;
            while(!valid){
                e_r=Math.floor(Math.random()*17); e_c=Math.floor(Math.random()*17); valid=true;
                if(e_r<rb_r+4 && e_r+4>rb_r && e_c<rb_c+4 && e_c+4>rb_c)valid=false;
                if(valid && e_r<bb_r+4 && e_r+4>bb_r && e_c<bb_c+4 && e_c+4>bb_c)valid=false;
                for(let j=0;j<this.elevatorCount && valid;j++) if(e_r<this.elevator_row[j]+4 && e_r+4>this.elevator_row[j] && e_c<this.elevator_col[j]+4 && e_c+4>this.elevator_col[j])valid=false;
            }
            this.elevator_row[i]=e_r; this.elevator_col[i]=e_c;
            for(let r=0;r<4;r++)for(let c=0;c<4;c++){
                let dC=this.getDownstairsCell(e_r+r,e_c+c); if(dC){dC.mark=Cell.ELEVATORROOM; if(r===3)dC.SouthBound=true; if(c===3)dC.EastBound=true;}
                let uC=this.getUpstairsCell(e_r+r,e_c+c); if(uC){uC.mark=Cell.ELEVATORROOM; if(r===3)uC.SouthBound=true; if(c===3)uC.EastBound=true;}
            }
            for(let k=0;k<4;k++){ let dC=this.getDownstairsCell(e_r+k,e_c-1); if(dC)dC.EastBound=true; dC=this.getDownstairsCell(e_r-1,e_c+k); if(dC)dC.SouthBound=true;
                                let uC=this.getUpstairsCell(e_r+k,e_c-1); if(uC)uC.EastBound=true; uC=this.getUpstairsCell(e_r-1,e_c+k); if(uC)uC.SouthBound=true;}
            for(let er=1;er<=2;er++)for(let ec=1;ec<=2;ec++){ // 2x2 center is ELEVATOR
                const dEC=this.getDownstairsCell(e_r+er,e_c+ec); if(dEC)dEC.mark=Cell.ELEVATOR;
                const uEC=this.getUpstairsCell(e_r+er,e_c+ec); if(uEC)uEC.mark=Cell.ELEVATOR;
            }
            this.elevatorCount++;
        }
        let altSet=false; for(let r=0;r<4&&!altSet;r++)for(let c=0;c<4&&!altSet;c++){const cl=this.getDownstairsCell(rb_r+1+r%2,rb_c+1+c%2);if(cl&&cl.mark===Cell.REDBASE){cl.mark=Cell.REDALTAR;altSet=true;}} // Try to place altar in 2x2 center of base
        if(!altSet)for(let r=0;r<4&&!altSet;r++)for(let c=0;c<4&&!altSet;c++){const cl=this.getDownstairsCell(rb_r+r,rb_c+c);if(cl&&cl.mark===Cell.REDBASE){cl.mark=Cell.REDALTAR;altSet=true;}}
        altSet=false; for(let r=0;r<4&&!altSet;r++)for(let c=0;c<4&&!altSet;c++){const cl=this.getUpstairsCell(bb_r+1+r%2,bb_c+1+c%2);if(cl&&cl.mark===Cell.BLUEBASE){cl.mark=Cell.BLUEALTAR;altSet=true;}}
        if(!altSet)for(let r=0;r<4&&!altSet;r++)for(let c=0;c<4&&!altSet;c++){const cl=this.getUpstairsCell(bb_r+r,bb_c+c);if(cl&&cl.mark===Cell.BLUEBASE){cl.mark=Cell.BLUEALTAR;altSet=true;}}
        let srcSet=0; const maxSrc=2;
        for(let k=0;k<400&&srcSet<maxSrc;k++){const r=Math.floor(Math.random()*20);const c=Math.floor(Math.random()*20); const cl=this.getDownstairsCell(r,c); if(cl&&cl.mark===Cell.NORMALROOM){cl.mark=Cell.REDSOURCE; srcSet++;}}
        srcSet=0; for(let k=0;k<400&&srcSet<maxSrc;k++){const r=Math.floor(Math.random()*20);const c=Math.floor(Math.random()*20); const cl=this.getUpstairsCell(r,c); if(cl&&cl.mark===Cell.NORMALROOM){cl.mark=Cell.BLUESOURCE; srcSet++;}}
        for(let i=0;i<800;i++){const cl=this.cells[i]; if(cl.mark===Cell.NORMALROOM){if(cl.m_column!==19&&Math.random()<0.4)cl.EastBound=true; if(cl.m_row!==19&&Math.random()<0.4)cl.SouthBound=true;}}
    }
}
