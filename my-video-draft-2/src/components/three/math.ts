// Rotation, lighting and projection that match CSS transforms (x right, y down, z toward the viewer).
export type Vec3 = readonly [number, number, number];
type Mat3 = readonly [Vec3, Vec3, Vec3];

const rad = (deg: number) => (deg * Math.PI) / 180;

const rotX = (deg: number): Mat3 => {
  const c = Math.cos(rad(deg));
  const s = Math.sin(rad(deg));
  return [[1, 0, 0], [0, c, -s], [0, s, c]];
};
const rotY = (deg: number): Mat3 => {
  const c = Math.cos(rad(deg));
  const s = Math.sin(rad(deg));
  return [[c, 0, s], [0, 1, 0], [-s, 0, c]];
};
const rotZ = (deg: number): Mat3 => {
  const c = Math.cos(rad(deg));
  const s = Math.sin(rad(deg));
  return [[c, -s, 0], [s, c, 0], [0, 0, 1]];
};

const mul = (a: Mat3, b: Mat3): Mat3 =>
  [0, 1, 2].map((r) => [0, 1, 2].map((c) => a[r][0] * b[0][c] + a[r][1] * b[1][c] + a[r][2] * b[2][c])) as unknown as Mat3;
const apply = (m: Mat3, v: Vec3): Vec3 => [
  m[0][0] * v[0] + m[0][1] * v[1] + m[0][2] * v[2],
  m[1][0] * v[0] + m[1][1] * v[1] + m[1][2] * v[2],
  m[2][0] * v[0] + m[2][1] * v[1] + m[2][2] * v[2],
];
const transpose = (m: Mat3): Mat3 => [0, 1, 2].map((r) => [0, 1, 2].map((c) => m[c][r])) as unknown as Mat3;
const dot = (a: Vec3, b: Vec3) => a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
const normalize = (v: Vec3): Vec3 => {
  const l = Math.hypot(v[0], v[1], v[2]) || 1;
  return [v[0] / l, v[1] / l, v[2] / l];
};

// Same order as `rotateX() rotateY() rotateZ()` in a transform string.
export const objectRotation = ([rx, ry, rz]: Vec3) => mul(mul(rotX(rx), rotY(ry)), rotZ(rz));
export const rotate = (rotation: Vec3, v: Vec3) => apply(objectRotation(rotation), v);

export type Camera = {
  // World point shown at the center of frame.
  readonly x?: number;
  readonly y?: number;
  readonly z?: number;
  // Degrees: orbit circles right (+) around the target, tilt looks down (+), roll banks clockwise (+).
  readonly orbit?: number;
  readonly tilt?: number;
  readonly roll?: number;
  // Pixels toward the target.
  readonly dolly?: number;
  readonly perspective?: number;
};

export const PERSPECTIVE = 1600;

const cameraRotation = ({ orbit = 0, tilt = 0, roll = 0 }: Camera) => mul(mul(rotZ(-roll), rotX(-tilt)), rotY(-orbit));

export const cameraTransform = (camera: Camera) => {
  const { x = 0, y = 0, z = 0, orbit = 0, tilt = 0, roll = 0, dolly = 0 } = camera;
  return `translateZ(${dolly}px) rotateZ(${-roll}deg) rotateX(${-tilt}deg) rotateY(${-orbit}deg) translate3d(${-x}px, ${-y}px, ${-z}px)`;
};

// World-space direction toward the camera.
export const viewDirection = (camera: Camera) => apply(transpose(cameraRotation(camera)), [0, 0, 1]);

// Screen position (frame pixels) and perspective scale of a world point.
export const project = (point: Vec3, camera: Camera, width = 1920, height = 1080) => {
  const { x = 0, y = 0, z = 0, dolly = 0, perspective = PERSPECTIVE } = camera;
  const v = apply(cameraRotation(camera), [point[0] - x, point[1] - y, point[2] - z]);
  const s = perspective / (perspective - (v[2] + dolly));
  return { x: width / 2 + v[0] * s, y: height / 2 + v[1] * s, scale: s };
};

// Upper-left key light, toward the viewer.
const KEY: Vec3 = normalize([-0.45, -0.7, 0.55]);
const FILL: Vec3 = normalize([0.7, 0.2, 0.4]);

export type Material = {
  readonly color: string;
  readonly ambient?: number;
  readonly diffuse?: number;
  readonly fill?: number;
  readonly specular?: number;
  readonly shininess?: number;
};

const parse = (hex: string): Vec3 => {
  const n = parseInt(hex.slice(1), 16);
  return [(n >> 16) & 255, (n >> 8) & 255, n & 255];
};

// Lambert key and fill plus a Blinn highlight; normal and view in world space.
export const shade = (material: Material, normal: Vec3, view: Vec3) => {
  const { color, ambient = 0.5, diffuse = 0.6, fill = 0.18, specular = 0.35, shininess = 20 } = material;
  const n = normalize(normal);
  const light = ambient + diffuse * Math.max(0, dot(n, KEY)) + fill * Math.max(0, dot(n, FILL));
  const highlight = specular * Math.max(0, dot(n, normalize([KEY[0] + view[0], KEY[1] + view[1], KEY[2] + view[2]]))) ** shininess;
  const [r, g, b] = parse(color).map((c) => Math.round(Math.min(255, c * light + 255 * highlight)));
  return `rgb(${r}, ${g}, ${b})`;
};
