import React from "react";
import { Wordmark } from "../Logo";
import type { MascotExpression } from "../Mascot";
import { Icon3D } from "./Icon3D";
import { Inline3D } from "./Stage";

// The lockup with a solid icon: `turn` spins it (degrees), `reveal` slides the
// wordmark out from beside it (0–1), `sheen` sweeps the glass as it lands.
export const Lockup3D: React.FC<{
  readonly iconSize: number;
  readonly wordSize: number;
  readonly turn: number;
  readonly tilt?: number;
  readonly iconScale?: number;
  readonly iconOpacity?: number;
  readonly sheen?: number;
  readonly reveal: number;
  readonly expression?: MascotExpression;
  readonly blendTo?: MascotExpression;
  readonly blend?: number;
}> = ({ iconSize, wordSize, turn, tilt = 0, iconScale = 1, iconOpacity = 1, sheen, reveal, expression, blendTo, blend }) => (
  <div style={{ display: "flex", alignItems: "center", gap: iconSize * 0.24 }}>
    <Inline3D width={iconSize} height={iconSize} style={{ opacity: iconOpacity }}>
      <Icon3D size={iconSize} rotation={[tilt, turn, 0]} scale={iconScale} sheen={sheen} expression={expression} blendTo={blendTo} blend={blend} />
    </Inline3D>
    <div style={{ overflow: "hidden", padding: "0.12em 0.08em 0.12em 0" }}>
      <div style={{ translate: `${-(1 - reveal) * 104}% 0`, filter: reveal < 1 ? `blur(${(1 - reveal) * 6}px)` : undefined }}>
        <Wordmark size={wordSize} />
      </div>
    </div>
  </div>
);
