// Global map variables
let mapData = [];
const MAP_WIDTH = 10;
const MAP_HEIGHT = 10;
const WALL_PROBABILITY = 0.3; // 30% chance for a wall

// Player state
let playerPosition = [1.5, 0.5, 1.5]; // Center of the player's AABB. Y=0.5 means player is from 0.0 to 1.0.
let playerYaw = 0.0;
let playerPitch = 0.0;
const moveSpeed = 0.1;
const rotateSpeed = 0.03;

// Player dimensions for AABB collision
const playerWidth = 0.4; // X-axis
const playerHeight = 1.0; // Y-axis
const playerDepth = 0.4;  // Z-axis

// Monster data
let monsters = [];
const MONSTER_COUNT = 3; // Reduced for simpler testing with keys/gates
const monsterWidth = 0.8;
const monsterHeight = 1.0;
const monsterDepth = 0.8;
const monsterSpeed = 0.02;

// Key and Gate data
let keys = [];
let gates = [];
const playerKeys = { red: false, yellow: false, blue: false };
const KEY_COLORS = {
    red: [1.0, 0.2, 0.2, 1.0],    // Slightly less saturated red
    yellow: [1.0, 1.0, 0.2, 1.0], // Slightly less saturated yellow
    blue: [0.2, 0.2, 1.0, 1.0]    // Slightly less saturated blue
};
const keyWidth = 0.3, keyHeight = 0.3, keyDepth = 0.3;
const gateWidth = 1.0, gateHeight = 1.0, gateDepth = 1.0; // Same as walls

// Keyboard input state
const keysPressed = {};

document.addEventListener('keydown', (event) => {
    keysPressed[event.key.toLowerCase()] = true;
    keysPressed[event.code] = true; // For arrow keys etc.
});

document.addEventListener('keyup', (event) => {
    keysPressed[event.key.toLowerCase()] = false;
    keysPressed[event.code] = false;
});


// Placeholder matrix utility object
const mat4 = {
    create: function() {
        // console.log("mat4.create called");
        // Return an identity matrix (16 elements)
        return [
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        ];
    },
    perspective: function(out, fovy, aspect, near, far) {
        // console.log("mat4.perspective called with:", fovy, aspect, near, far);
        const f = 1.0 / Math.tan(fovy / 2);
        out[0] = f / aspect; out[1] = 0; out[2] = 0; out[3] = 0;
        out[4] = 0; out[5] = f; out[6] = 0; out[7] = 0;
        out[8] = 0; out[9] = 0; out[10] = (far + near) / (near - far); out[11] = -1;
        out[12] = 0; out[13] = 0; out[14] = (2 * far * near) / (near - far); out[15] = 0;
        return out;
    },
    translate: function(out, a, v) {
        // console.log("mat4.translate called with vector:", v);
        // Simple translation, assuming 'a' is the matrix to translate and 'out' is the result.
        let x = v[0], y = v[1], z = v[2];
        let a00, a01, a02, a03;
        let a10, a11, a12, a13;
        let a20, a21, a22, a23;

        if (a === out) {
            out[12] = a[0] * x + a[4] * y + a[8] * z + a[12];
            out[13] = a[1] * x + a[5] * y + a[9] * z + a[13];
            out[14] = a[2] * x + a[6] * y + a[10] * z + a[14];
            out[15] = a[3] * x + a[7] * y + a[11] * z + a[15];
        } else {
            a00 = a[0]; a01 = a[1]; a02 = a[2]; a03 = a[3];
            a10 = a[4]; a11 = a[5]; a12 = a[6]; a13 = a[7];
            a20 = a[8]; a21 = a[9]; a22 = a[10]; a23 = a[11];

            out[0] = a00; out[1] = a01; out[2] = a02; out[3] = a03;
            out[4] = a10; out[5] = a11; out[6] = a12; out[7] = a13;
            out[8] = a20; out[9] = a21; out[10] = a22; out[11] = a23;

            out[12] = a00 * x + a10 * y + a20 * z + a[12];
            out[13] = a01 * x + a11 * y + a21 * z + a[13];
            out[14] = a02 * x + a12 * y + a22 * z + a[14];
            out[15] = a03 * x + a13 * y + a23 * z + a[15];
        }
        return out;
    },
    identity: function(out) {
        // console.log("mat4.identity called");
        out[0] = 1; out[1] = 0; out[2] = 0; out[3] = 0;
        out[4] = 0; out[5] = 1; out[6] = 0; out[7] = 0;
        out[8] = 0; out[9] = 0; out[10] = 1; out[11] = 0;
        out[12] = 0; out[13] = 0; out[14] = 0; out[15] = 1;
        return out;
    },
    rotate: function(out, a, rad, axis) {
        // Basic AABB collision check
        // rect = { x, y, z, width, height, depth } (center coordinates)
        let x_axis_val = axis[0], y_axis_val = axis[1], z_axis_val = axis[2];
        let len = Math.sqrt(x_axis_val * x_axis_val + y_axis_val * y_axis_val + z_axis_val * z_axis_val);
        let s, c, t_val; // Renamed t to t_val
        let a00, a01, a02, a03; // Matrix elements
        let a10, a11, a12, a13;
        let a20, a21, a22, a23;
        let b00, b01, b02; // Rotation matrix elements
        let b10, b11, b12;
        let b20, b21, b22;

        if (len < 0.00001) { return null; }

        len = 1 / len;
        x_axis_val *= len;
        y_axis_val *= len;
        z_axis_val *= len;

        s = Math.sin(rad);
        c = Math.cos(rad);
        t_val = 1 - c;

        a00 = a[0]; a01 = a[1]; a02 = a[2]; a03 = a[3];
        a10 = a[4]; a11 = a[5]; a12 = a[6]; a13 = a[7];
        a20 = a[8]; a21 = a[9]; a22 = a[10]; a23 = a[11];

        b00 = x_axis_val * x_axis_val * t_val + c;
        b01 = y_axis_val * x_axis_val * t_val + z_axis_val * s;
        b02 = z_axis_val * x_axis_val * t_val - y_axis_val * s;

        b10 = x_axis_val * y_axis_val * t_val - z_axis_val * s;
        b11 = y_axis_val * y_axis_val * t_val + c;
        b12 = z_axis_val * y_axis_val * t_val + x_axis_val * s;

        b20 = x_axis_val * z_axis_val * t_val + y_axis_val * s;
        b21 = y_axis_val * z_axis_val * t_val - x_axis_val * s;
        b22 = z_axis_val * z_axis_val * t_val + c;

        out[0] = a00 * b00 + a10 * b01 + a20 * b02; out[1] = a01 * b00 + a11 * b01 + a21 * b02;
        out[2] = a02 * b00 + a12 * b01 + a22 * b02; out[3] = a03 * b00 + a13 * b01 + a23 * b02;
        out[4] = a00 * b10 + a10 * b11 + a20 * b12; out[5] = a01 * b10 + a11 * b11 + a21 * b12;
        out[6] = a02 * b10 + a12 * b11 + a22 * b12; out[7] = a03 * b10 + a13 * b11 + a23 * b12;
        out[8] = a00 * b20 + a10 * b21 + a20 * b22; out[9] = a01 * b20 + a11 * b21 + a21 * b22;
        out[10] = a02 * b20 + a12 * b21 + a22 * b22; out[11] = a03 * b20 + a13 * b21 + a23 * b22;

        if (a !== out) {
            out[12] = a[12]; out[13] = a[13]; out[14] = a[14]; out[15] = a[15];
        }
        return out;
    },
    scale: function(out, mat_a, vec_s) {
        out[0] = mat_a[0] * vec_s[0];
        out[1] = mat_a[1] * vec_s[0];
        out[2] = mat_a[2] * vec_s[0];
        out[3] = mat_a[3] * vec_s[0];
        out[4] = mat_a[4] * vec_s[1];
        out[5] = mat_a[5] * vec_s[1];
        out[6] = mat_a[6] * vec_s[1];
        out[7] = mat_a[7] * vec_s[1];
        out[8] = mat_a[8] * vec_s[2];
        out[9] = mat_a[9] * vec_s[2];
        out[10] = mat_a[10] * vec_s[2];
        out[11] = mat_a[11] * vec_s[2];
        out[12] = mat_a[12];
        out[13] = mat_a[13];
        out[14] = mat_a[14];
        out[15] = mat_a[15];
        return out;
    }
};

function checkCollision(rect1, rect2) {
    // rect = { x, y, z, width, height, depth } (center coordinates)
    const overlapX = Math.abs(rect1.x - rect2.x) * 2 < (rect1.width + rect2.width);
    const overlapY = Math.abs(rect1.y - rect2.y) * 2 < (rect1.height + rect2.height);
    const overlapZ = Math.abs(rect1.z - rect2.z) * 2 < (rect1.depth + rect2.depth);
    return overlapX && overlapY && overlapZ;
}

function updatePlayerState() {
    const cosYaw = Math.cos(playerYaw);
    const sinYaw = Math.sin(playerYaw);

    let deltaX = 0;
    let deltaZ = 0;

    if (keysPressed['w']) {
        deltaX += sinYaw * moveSpeed;
        deltaZ -= cosYaw * moveSpeed;
    }
    if (keysPressed['s']) {
        deltaX -= sinYaw * moveSpeed;
        deltaZ += cosYaw * moveSpeed;
    }
    if (keysPressed['a']) {
        deltaX -= cosYaw * moveSpeed;
        deltaZ -= sinYaw * moveSpeed;
    }
    if (keysPressed['d']) {
        deltaX += cosYaw * moveSpeed;
        deltaZ += sinYaw * moveSpeed;
    }

    // Store current position to revert if collision occurs on an axis
    const originalPlayerX = playerPosition[0];
    const originalPlayerZ = playerPosition[2];

    // --- X-axis movement and collision ---
    if (deltaX !== 0) {
        const potentialPlayerX = playerPosition[0] + deltaX;
        const playerAABB_X = {
            x: potentialPlayerX,
            y: playerPosition[1],
            z: playerPosition[2], // Use current Z for this check
            width: playerWidth,
            height: playerHeight,
            depth: playerDepth
        };

        let collisionX = false;
        for (let r = 0; r < MAP_HEIGHT; r++) {
            for (let c = 0; c < MAP_WIDTH; c++) {
                if (mapData[r][c] === 1) { // If it's a wall
                    const wallAABB = {
                        x: c, // Center of the wall cube is at (c, 0, r)
                        y: 0.0,
                        z: r,
                        width: 1, height: 1, depth: 1
                    };
                    if (checkCollision(playerAABB_X, wallAABB)) {
                        collisionX = true;
                        break;
                    }
                }
            }
            if (collisionX) break;
        }

        if (!collisionX) {
            playerPosition[0] = potentialPlayerX;
        }
    }

    // --- Z-axis movement and collision ---
    if (deltaZ !== 0) {
        const potentialPlayerZ = playerPosition[2] + deltaZ;
        const playerAABB_Z = {
            x: playerPosition[0], // Use potentially updated X
            y: playerPosition[1],
            z: potentialPlayerZ,
            width: playerWidth,
            height: playerHeight,
            depth: playerDepth
        };

        let collisionZ = false;
        for (let r = 0; r < MAP_HEIGHT; r++) {
            for (let c = 0; c < MAP_WIDTH; c++) {
                if (mapData[r][c] === 1) { // If it's a wall
                    const wallAABB = {
                        x: c, // Center of the wall cube is at (c, 0, r)
                        y: 0.0,
                        z: r,
                        width: 1, height: 1, depth: 1
                    };
                    if (checkCollision(playerAABB_Z, wallAABB)) {
                        collisionZ = true;
                        break;
                    }
                }
            }
            if (collisionZ) break;
        }

        if (!collisionZ) {
            playerPosition[2] = potentialPlayerZ;
        }
    }

    // Rotation (no collision check needed for this basic implementation)
    if (keysPressed['arrowleft']) {
        playerYaw += rotateSpeed;
    }
    if (keysPressed['arrowright']) {
        playerYaw -= rotateSpeed;
    }
    if (keysPressed['arrowup']) {
        playerPitch += rotateSpeed;
        playerPitch = Math.max(-Math.PI / 2, Math.min(Math.PI / 2, playerPitch)); // Clamp pitch
    }
    if (keysPressed['arrowdown']) {
        playerPitch -= rotateSpeed;
        playerPitch = Math.max(-Math.PI / 2, Math.min(Math.PI / 2, playerPitch)); // Clamp pitch
    }
}

function generateMap() {
    mapData = [];
    for (let y = 0; y < MAP_HEIGHT; y++) {
        const row = [];
        for (let x = 0; x < MAP_WIDTH; x++) {
            if (x < 2 && y < 2) { // Make a 2x2 clear area at (0,0) (1,0) (0,1) (1,1) for player start
                row.push(0);
            } else {
                row.push(Math.random() < WALL_PROBABILITY ? 1 : 0);
            }
        }
        mapData.push(row);
    }
    console.log("Map generated. Player starts near [1.5, 0.5, 1.5]");
}

function spawnMonsters(count) {
    monsters = [];
    const playerInitialX = 1; //Approximate player start cell X
    const playerInitialZ = 1; //Approximate player start cell Z
    const minSpawnDistSq = 3 * 3; // Minimum squared distance from player start

    for (let i = 0; i < count; i++) {
        let cellX, cellZ;
        let validSpawn = false;
        let attempts = 0;
        while (!validSpawn && attempts < 100) { // Limit attempts to prevent infinite loop
            cellX = Math.floor(Math.random() * MAP_WIDTH);
            cellZ = Math.floor(Math.random() * MAP_HEIGHT);

            const distSq = (cellX - playerInitialX) * (cellX - playerInitialX) + (cellZ - playerInitialZ) * (cellZ - playerInitialZ);

            if (mapData[cellZ][cellX] === 0 && distSq > minSpawnDistSq) { // Check if cell is empty and not too close
                validSpawn = true;
            }
            attempts++;
        }

        if (validSpawn) {
            monsters.push({
                x: cellX + 0.5, // Center of the cell
                y: 0.5,         // Center of height, assuming monster is 1 unit high on the floor
                z: cellZ + 0.5, // Center of the cell
                width: monsterWidth,
                height: monsterHeight,
                depth: monsterDepth,
                color: [1.0, 0.0, 0.0, 1.0], // Red
                speed: monsterSpeed
            });
        } else {
            console.warn("Could not find a valid spawn location for a monster after 100 attempts.");
        }
    }
    console.log(monsters.length + " monsters spawned.");
}

function spawnKeysAndGates() {
    keys = [];
    gates = [];
    let keyIdCounter = 0;
    let gateIdCounter = 0;

    const occupiedCells = new Set(); // To avoid spawning items on top of each other in this cycle

    for (const colorName in KEY_COLORS) {
        const colorValue = KEY_COLORS[colorName];
        let attempts = 0;

        // Spawn Key
        let keySpawned = false;
        while (!keySpawned && attempts < 100) {
            const c = Math.floor(Math.random() * MAP_WIDTH);
            const r = Math.floor(Math.random() * MAP_HEIGHT);
            const cellKey = `${c}-${r}`;
            if (mapData[r][c] === 0 && !occupiedCells.has(cellKey)) { // Empty cell
                keys.push({
                    x: c + 0.5, y: 0.25, z: r + 0.5, // Center of cell, Y slightly floating
                    colorName: colorName, colorValue: colorValue,
                    id: `key-${keyIdCounter++}`, isCollected: false,
                    width: keyWidth, height: keyHeight, depth: keyDepth
                });
                occupiedCells.add(cellKey);
                keySpawned = true;
                console.log(`Spawned ${colorName} key at (${c}, ${r})`);
            }
            attempts++;
        }
        if (!keySpawned) console.warn(`Could not spawn ${colorName} key.`);

        // Spawn Gate
        attempts = 0;
        let gateSpawned = false;
        while (!gateSpawned && attempts < 100) {
            const c = Math.floor(Math.random() * MAP_WIDTH);
            const r = Math.floor(Math.random() * MAP_HEIGHT);
            const cellKey = `${c}-${r}`;
            // Ensure it's a wall and not where the player starts or too close, and not already a gate
            if (mapData[r][c] === 1 && (c > 2 || r > 2) && !occupiedCells.has(cellKey)) {
                gates.push({
                    x: c + 0.5, y: 0.0, z: r + 0.5, // Center of the wall block
                    colorName: colorName, colorValue: colorValue,
                    id: `gate-${gateIdCounter++}`, isOpen: false,
                    mapR: r, mapC: c,
                    width: gateWidth, height: gateHeight, depth: gateDepth
                });
                // Don't modify mapData here, drawScene will handle gate appearance
                occupiedCells.add(cellKey);
                gateSpawned = true;
                console.log(`Spawned ${colorName} gate at wall (${c}, ${r})`);
            }
            attempts++;
        }
        if (!gateSpawned) console.warn(`Could not spawn ${colorName} gate.`);
    }
}


function updateMonsters() {
    const playerAABB = {
        x: playerPosition[0], y: playerPosition[1], z: playerPosition[2],
        width: playerWidth, height: playerHeight, depth: playerDepth
    };

    for (let i = 0; i < monsters.length; i++) {
        const monster = monsters[i];
        const dirX = playerPosition[0] - monster.x; // AI Movement
        const dirZ = playerPosition[2] - monster.z;
        const length = Math.sqrt(dirX * dirX + dirZ * dirZ);
        let m_deltaX = 0, m_deltaZ = 0;
        if (length > 0.1) {
            m_deltaX = (dirX / length) * monster.speed;
            m_deltaZ = (dirZ / length) * monster.speed;
        }

        if (m_deltaX !== 0) { // Monster X-axis collision
            const pMonsterX = monster.x + m_deltaX;
            const mAABB_X = { x: pMonsterX, y: monster.y, z: monster.z, width: monster.width, height: monster.height, depth: monster.depth };
            let mCollX = false;
            for (let r_ = 0; r_ < MAP_HEIGHT; r_++) {
                for (let c_ = 0; c_ < MAP_WIDTH; c_++) {
                    if (mapData[r_][c_] === 1) {
                        const wallAABB = { x: c_, y: 0.0, z: r_, width: 1, height: 1, depth: 1 };
                        if (checkCollision(mAABB_X, wallAABB)) { mCollX = true; break; }
                    }
                }
                if (mCollX) break;
            }
            if (!mCollX) monster.x = pMonsterX;
        }

        if (m_deltaZ !== 0) { // Monster Z-axis collision
            const pMonsterZ = monster.z + m_deltaZ;
            const mAABB_Z = { x: monster.x, y: monster.y, z: pMonsterZ, width: monster.width, height: monster.height, depth: monster.depth };
            let mCollZ = false;
            for (let r_ = 0; r_ < MAP_HEIGHT; r_++) {
                for (let c_ = 0; c_ < MAP_WIDTH; c_++) {
                    if (mapData[r_][c_] === 1) {
                        const wallAABB = { x: c_, y: 0.0, z: r_, width: 1, height: 1, depth: 1 };
                        if (checkCollision(mAABB_Z, wallAABB)) { mCollZ = true; break; }
                    }
                }
                if (mCollZ) break;
            }
            if (!mCollZ) monster.z = pMonsterZ;
        }

        const monsterAABB = { x: monster.x, y: monster.y, z: monster.z, width: monster.width, height: monster.height, depth: monster.depth };
        if (checkCollision(playerAABB, monsterAABB)) {
            console.log("Player touched by monster!");
        }
    }
}

function updateGameLogic() {
    const playerAABB = {
        x: playerPosition[0], y: playerPosition[1], z: playerPosition[2],
        width: playerWidth, height: playerHeight, depth: playerDepth
    };

    // Key Collection
    for (const key of keys) {
        if (!key.isCollected) {
            const keyAABB = { x: key.x, y: key.y, z: key.z, width: key.width, height: key.height, depth: key.depth };
            if (checkCollision(playerAABB, keyAABB)) {
                key.isCollected = true;
                playerKeys[key.colorName] = true;
                console.log(`Collected ${key.colorName} key!`);
            }
        }
    }

    // Gate Opening
    for (const gate of gates) {
        if (!gate.isOpen) {
            // Gate AABB is based on its map coordinates, effectively a wall
            const gateAABB = { x: gate.mapC + 0.5, y: 0.0, z: gate.mapR + 0.5, width: 1, height: 1, depth: 1 };
            if (checkCollision(playerAABB, gateAABB)) { // Player needs to be close to the gate's actual block
                if (playerKeys[gate.colorName]) {
                    gate.isOpen = true;
                    mapData[gate.mapR][gate.mapC] = 0; // Make gate passable in mapData
                    console.log(`Opened ${gate.colorName} gate!`);
                    if (checkLevelFinish()) {
                        console.log("Level Complete! All gates opened.");
                        // Potentially trigger next level or game end state
                    }
                } else {
                    console.log(`You need the ${gate.colorName} key to open this gate.`);
                }
            }
        }
    }
}

function checkLevelFinish() {
    for (const gate of gates) {
        if (!gate.isOpen) return false;
    }
    return true;
}

// Declare gl, programInfo, buffers in a wider scope
let gl;
let programInfo;
let buffers;
// let mapData is already global

window.onload = function() {
    const canvas = document.getElementById('glCanvas');

    if (!canvas) {
        console.error('Canvas element not found!');
        alert('Error: Canvas element not found! Please ensure a canvas element with id "glCanvas" exists in your HTML.');
        return;
    }

    const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');

    if (!gl) {
        console.error('WebGL not supported!');
        alert('Error: WebGL is not supported by your browser. Please try a different browser or update your current one.');
        return;
    }

    // Set clear color to black, fully opaque
    gl.clearColor(0.0, 0.0, 0.0, 1.0);
    // Clear the color buffer with specified clear color
    gl.clear(gl.COLOR_BUFFER_BIT);

    console.log("WebGL initialized and canvas cleared.");

    gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');

    if (!gl) {
        console.error('WebGL not supported!');
        alert('Error: WebGL is not supported by your browser. Please try a different browser or update your current one.');
        return;
    }

    // Set clear color to black, fully opaque
    gl.clearColor(0.0, 0.0, 0.0, 1.0);
    // Clear the color buffer with specified clear color
    gl.clear(gl.COLOR_BUFFER_BIT);

    console.log("WebGL initialized and canvas cleared.");

    // Vertex shader source code
    const vsSource = `
        attribute vec3 aVertexPosition;
        uniform mat4 uProjectionMatrix;
        uniform mat4 uViewMatrix;
        uniform mat4 uModelMatrix;
        void main(void) {
            gl_Position = uProjectionMatrix * uViewMatrix * uModelMatrix * vec4(aVertexPosition, 1.0);
        }
    `;

    // Fragment shader source code
    const fsSource = `
        precision mediump float; // Required for WebGL
        uniform vec4 uColor;
        void main(void) {
            gl_FragColor = uColor;
        }
    `;

    const shaderProgram = initShaderProgram(gl, vsSource, fsSource);

    if (!shaderProgram) {
        console.error('Failed to initialize shader program.');
        alert('Error: Could not initialize shader program. Check console for details.');
        return;
    }

    // Store the program in a global or accessible variable if needed later
    console.log("Shader program initialized successfully.");

    // Store the program in a global or accessible variable if needed later
    console.log("Shader program initialized successfully.");

    programInfo = {
        program: shaderProgram,
        attribLocations: {
            vertexPosition: gl.getAttribLocation(shaderProgram, 'aVertexPosition'),
        },
        uniformLocations: {
            projectionMatrix: gl.getUniformLocation(shaderProgram, 'uProjectionMatrix'),
            viewMatrix: gl.getUniformLocation(shaderProgram, 'uViewMatrix'),
            modelMatrix: gl.getUniformLocation(shaderProgram, 'uModelMatrix'),
            color: gl.getUniformLocation(shaderProgram, 'uColor'), // Added color uniform
        },
    };

    // Error checks for all locations
    if (programInfo.attribLocations.vertexPosition === -1) {
        console.error('Attribute aVertexPosition not found.'); return;
    }
    if (!programInfo.uniformLocations.projectionMatrix) {
        console.error('Uniform uProjectionMatrix not found.'); return;
    }
    if (!programInfo.uniformLocations.viewMatrix) {
        console.error('Uniform uViewMatrix not found.'); return;
    }
    if (!programInfo.uniformLocations.modelMatrix) {
        console.error('Uniform uModelMatrix not found.'); return;
    }
    if (!programInfo.uniformLocations.color) {
        console.error('Uniform uColor not found.'); return;
    }

    gl.enableVertexAttribArray(programInfo.attribLocations.vertexPosition);
    console.log("Vertex attribute and uniform locations obtained.");

    buffers = initBuffers(gl);
    if (!buffers) {
        console.error('Failed to initialize buffers.');
        alert('Error: Could not initialize buffers for the cube.');
        return;
    }
    console.log("Buffers initialized successfully.");

    generateMap();
    spawnKeysAndGates(); // Spawn keys and gates
    spawnMonsters(MONSTER_COUNT);

    requestAnimationFrame(gameLoop);
    console.log("Game loop started.");
};

function gameLoop() {
    updatePlayerState();
    updateMonsters();
    updateGameLogic(); // Handle key/gate logic
    drawScene(gl, programInfo, buffers, mapData);
    requestAnimationFrame(gameLoop);
}

function initBuffers(gl) {
    // Create a buffer for the cube's vertex positions.
    const positionBuffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer);

    // Now create an array of positions for the cube.
    // 8 vertices for a cube
    const positions = [
        // Front face
        -0.5, -0.5,  0.5,
         0.5, -0.5,  0.5,
         0.5,  0.5,  0.5,
        -0.5,  0.5,  0.5,

        // Back face
        -0.5, -0.5, -0.5,
        -0.5,  0.5, -0.5,
         0.5,  0.5, -0.5,
         0.5, -0.5, -0.5,

        // Top face
        -0.5,  0.5, -0.5,
        -0.5,  0.5,  0.5,
         0.5,  0.5,  0.5,
         0.5,  0.5, -0.5,

        // Bottom face
        -0.5, -0.5, -0.5,
         0.5, -0.5, -0.5,
         0.5, -0.5,  0.5,
        -0.5, -0.5,  0.5,

        // Right face
         0.5, -0.5, -0.5,
         0.5,  0.5, -0.5,
         0.5,  0.5,  0.5,
         0.5, -0.5,  0.5,

        // Left face
        -0.5, -0.5, -0.5,
        -0.5, -0.5,  0.5,
        -0.5,  0.5,  0.5,
        -0.5,  0.5, -0.5,
    ];

    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(positions), gl.STATIC_DRAW);

    // Create a buffer for the cube's indices.
    const indexBuffer = gl.createBuffer();
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, indexBuffer);

    // This array defines each face as two triangles, using the
    // indices into the vertex array to specify each triangle's position.
    const indices = [
        0,  1,  2,      0,  2,  3,    // front
        4,  5,  6,      4,  6,  7,    // back
        8,  9,  10,     8,  10, 11,   // top
        12, 13, 14,     12, 14, 15,   // bottom
        16, 17, 18,     16, 18, 19,   // right
        20, 21, 22,     20, 22, 23,   // left
    ];

    gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(indices), gl.STATIC_DRAW);

    return {
        position: positionBuffer,
        indices: indexBuffer,
        vertexCount: indices.length,
    };
}

function drawScene(gl, currentProgramInfo, currentBuffers, currentMapData) {
    gl.clearColor(0.1, 0.1, 0.1, 1.0);  // Clear to a slightly different gray
    gl.clearDepth(1.0);                 // Clear everything
    gl.enable(gl.DEPTH_TEST);           // Enable depth testing
    gl.depthFunc(gl.LEQUAL);            // Near things obscure far things

    gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

    const fieldOfView = 45 * Math.PI / 180;   // in radians
    const aspect = gl.canvas.clientWidth / gl.canvas.clientHeight;
    const zNear = 0.1;
    const zFar = 100.0;
    const projectionMatrix = mat4.create();
    mat4.perspective(projectionMatrix, fieldOfView, aspect, zNear, zFar);

    const viewMatrix = mat4.create();
    mat4.identity(viewMatrix);
    // Apply rotations:
    // Pitch rotation (around X-axis of camera)
    mat4.rotate(viewMatrix, viewMatrix, -playerPitch, [1, 0, 0]);
    // Yaw rotation (around Y-axis of world, effectively camera's local Y before translation)
    mat4.rotate(viewMatrix, viewMatrix, -playerYaw, [0, 1, 0]);
    // Apply translation (move the world away from the camera)
    mat4.translate(viewMatrix, viewMatrix, [-playerPosition[0], -playerPosition[1], -playerPosition[2]]);


    gl.useProgram(currentProgramInfo.program);

    gl.uniformMatrix4fv(
        currentProgramInfo.uniformLocations.projectionMatrix,
        false,
        projectionMatrix);

    gl.uniformMatrix4fv(
        currentProgramInfo.uniformLocations.viewMatrix,
        false,
        viewMatrix);

    gl.bindBuffer(gl.ARRAY_BUFFER, currentBuffers.position);
    gl.vertexAttribPointer(
        currentProgramInfo.attribLocations.vertexPosition,
        3, gl.FLOAT, false, 0, 0);
    // gl.enableVertexAttribArray is done once at init

    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, currentBuffers.indices);

    // Draw Walls and Gates
    const defaultWallColor = [0.5, 0.5, 0.5, 1.0];
    for (let r = 0; r < MAP_HEIGHT; r++) {
        for (let c = 0; c < MAP_WIDTH; c++) {
            let isGate = false;
            let gateColorToUse = defaultWallColor;

            for (const gate of gates) {
                if (gate.mapR === r && gate.mapC === c) {
                    if (!gate.isOpen) {
                        isGate = true;
                        gateColorToUse = gate.colorValue;
                    } else {
                        // Gate is open, mapData[r][c] should be 0, so it won't be drawn as wall
                    }
                    break;
                }
            }

            if (mapData[r][c] === 1 || isGate) { // If it's a wall or an unopen gate
                gl.uniform4fv(currentProgramInfo.uniformLocations.color, isGate ? gateColorToUse : defaultWallColor);
                
                const modelMatrix = mat4.create();
                mat4.identity(modelMatrix);
                mat4.translate(modelMatrix, modelMatrix, [c, 0.0, r]);

                gl.uniformMatrix4fv(
                    currentProgramInfo.uniformLocations.modelMatrix,
                    false,
                    modelMatrix);
                gl.drawElements(gl.TRIANGLES, currentBuffers.vertexCount, gl.UNSIGNED_SHORT, 0);
            }
        }
    }

    // Draw Monsters
    for (let i = 0; i < monsters.length; i++) {
        const monster = monsters[i];
        gl.uniform4fv(currentProgramInfo.uniformLocations.color, monster.color);
        const modelMatrix = mat4.create();
        mat4.identity(modelMatrix);
        mat4.translate(modelMatrix, modelMatrix, [monster.x, monster.y, monster.z]);
        gl.uniformMatrix4fv(currentProgramInfo.uniformLocations.modelMatrix, false, modelMatrix);
        gl.drawElements(gl.TRIANGLES, currentBuffers.vertexCount, gl.UNSIGNED_SHORT, 0);
    }

    // Draw Keys
    for (const key of keys) {
        if (!key.isCollected) {
            gl.uniform4fv(currentProgramInfo.uniformLocations.color, key.colorValue);
            const modelMatrix = mat4.create();
            mat4.identity(modelMatrix);
            mat4.translate(modelMatrix, modelMatrix, [key.x, key.y, key.z]);
            mat4.scale(modelMatrix, modelMatrix, [key.width/gateWidth, key.height/gateHeight, key.depth/gateDepth]); // Scale relative to full block size
            gl.uniformMatrix4fv(currentProgramInfo.uniformLocations.modelMatrix, false, modelMatrix);
            gl.drawElements(gl.TRIANGLES, currentBuffers.vertexCount, gl.UNSIGNED_SHORT, 0);
        }
    }
}

// Function to initialize a shader program, so it can be called from the main code
function initShaderProgram(gl, vsSource, fsSource) {
    const vertexShader = loadShader(gl, gl.VERTEX_SHADER, vsSource);
    const fragmentShader = loadShader(gl, gl.FRAGMENT_SHADER, fsSource);

    // Create the shader program
    const shaderProgram = gl.createProgram();
    gl.attachShader(shaderProgram, vertexShader);
    gl.attachShader(shaderProgram, fragmentShader);
    gl.linkProgram(shaderProgram);

    // Check if it linked successfully
    if (!gl.getProgramParameter(shaderProgram, gl.LINK_STATUS)) {
        console.error('Unable to initialize the shader program: ' + gl.getProgramInfoLog(shaderProgram));
        alert('Error: Shader program linking failed: ' + gl.getProgramInfoLog(shaderProgram));
        gl.deleteProgram(shaderProgram); // Clean up if linking failed
        return null;
    }

    return shaderProgram;
}

// Function to load a shader, compile it, and check for errors
function loadShader(gl, type, source) {
    const shader = gl.createShader(type);

    // Send the source to the shader object
    gl.shaderSource(shader, source);

    // Compile the shader program
    gl.compileShader(shader);

    // See if it compiled successfully
    if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
        const shaderType = type === gl.VERTEX_SHADER ? "vertex" : "fragment";
        console.error('An error occurred compiling the ' + shaderType + ' shader: ' + gl.getShaderInfoLog(shader));
        alert('Error: Shader compilation failed for ' + shaderType + ' shader: ' + gl.getShaderInfoLog(shader));
        gl.deleteShader(shader);
        return null;
    }

    return shader;
}
