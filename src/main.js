// Initialize scene, camera, and renderer
const scene = new THREE.Scene();
const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 10000); // Increased far plane
const renderer = new THREE.WebGLRenderer({ canvas: document.getElementById('gameCanvas') });
renderer.setSize(window.innerWidth, window.innerHeight);
document.body.appendChild(renderer.domElement);

// Position the camera for a good overview
// Labyrinth dimensions: 20 cells * 10 units/cell = 200 units wide/deep.
// Two levels high, each level CELLHEIGHT = 10 units + floor/ceiling.
// Center of labyrinth is roughly (100, CELLHEIGHT, 100)
camera.position.set(Labyrinth.CELLWIDTH * 10, Labyrinth.CELLHEIGHT * 5, Labyrinth.CELLWIDTH * 25); // Position further back and higher
camera.lookAt(Labyrinth.CELLWIDTH * 10, Labyrinth.CELLHEIGHT, Labyrinth.CELLWIDTH * 10); // Look at center of ground floor

// Add OrbitControls
// const controls = new THREE.OrbitControls(camera, renderer.domElement); // Disable OrbitControls
// controls.enableDamping = true;
// controls.dampingFactor = 0.25;
// controls.screenSpacePanning = false;
// controls.maxPolarAngle = Math.PI / 2;

// Import and create Labyrinth
import { Labyrinth } from './Labyrinth.js';
const labyrinth = new Labyrinth(scene); // Pass the scene

// Import and create PlayerCamera
import { PlayerCamera } from './PlayerCamera.js';
const playerCamera = new PlayerCamera(camera, labyrinth);

// Clock for deltaTime
const clock = new THREE.Clock();

// Render loop
function animate() {
    requestAnimationFrame(animate);
    const deltaTime = clock.getDelta();

    playerCamera.update(deltaTime);
    labyrinth.updateElevators(deltaTime, playerCamera); 
    labyrinth.updateRockets(deltaTime); // Update active rockets
    // controls.update(); // Disabled

    renderer.render(scene, camera);
}
animate();

// Handle window resize
window.addEventListener('resize', () => {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
    // Note: PlayerCamera does not automatically handle aspect ratio changes for its projection matrix.
    // The Three.js camera object it manipulates will have its projection matrix updated here.
}, false);

// Add some basic lighting
const ambientLight = new THREE.AmbientLight(0x404040); // soft white light
scene.add(ambientLight);
const directionalLight = new THREE.DirectionalLight(0xffffff, 0.5);
directionalLight.position.set(1, 1, 1).normalize();
scene.add(directionalLight);
