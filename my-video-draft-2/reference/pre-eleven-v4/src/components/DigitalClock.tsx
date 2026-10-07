import React from "react";
import { FONT } from "../brand/tokens";

const SEGMENTS = [
  "18,5 78,5 87,14 78,23 18,23 9,14",
  "82,27 91,18 100,27 100,91 91,100 82,91",
  "82,109 91,100 100,109 100,173 91,182 82,173",
  "18,177 78,177 87,186 78,195 18,195 9,186",
  "0,109 9,100 18,109 18,173 9,182 0,173",
  "0,27 9,18 18,27 18,91 9,100 0,91",
  "18,91 78,91 87,100 78,109 18,109 9,100",
];
const DIGITS: Record<string, readonly number[]> = { "2": [0, 1, 6, 4, 3], "0": [0, 1, 2, 3, 4, 5], "7": [0, 1, 2] };

export const DigitalClock: React.FC = () => (
  <div style={{ padding: "72px 88px", borderRadius: 44, background: "linear-gradient(145deg, #161B27, #080B11)", boxShadow: "inset 0 0 0 2px #2B3342, 0 35px 110px rgba(0,0,0,0.7), 0 0 130px rgba(147,169,226,0.12)" }}>
    <svg width={1080} height={400} viewBox="0 0 540 200" role="img" aria-label="Digital clock reading 2:07 AM">
      {([["2", 0], ["0", 164], ["7", 280]] as const).map(([digit, x]) => (
        <g key={x} transform={"translate(" + x + " 0)"}>
          {SEGMENTS.map((points, i) => <polygon key={i} points={points} fill={DIGITS[digit].includes(i) ? "#BED1FF" : "#1E2635"} style={DIGITS[digit].includes(i) ? { filter: "drop-shadow(0 0 4px rgba(147,169,226,0.6))" } : undefined} />)}
        </g>
      ))}
      <g fill="#BED1FF" style={{ filter: "drop-shadow(0 0 4px #93A9E2)" }}>
        <rect x={126} y={56} width={17} height={17} rx={4} />
        <rect x={126} y={124} width={17} height={17} rx={4} />
      </g>
      <text x={418} y={174} fill="#93A9E2" fontFamily={FONT} fontSize={45} fontWeight={700} letterSpacing={3}>AM</text>
    </svg>
  </div>
);
