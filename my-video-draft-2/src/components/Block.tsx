import React from "react";
import type { TextBlock } from "../choreography";
import { Phrase, Words } from "./Kinetic";

// A text block placed and timed by src/choreography.ts.
export const Block: React.FC<{
  readonly block: TextBlock;
  readonly swing?: boolean;
  // false: the block leaves with its container instead of tipping back.
  readonly exit?: boolean;
  readonly align?: "left" | "center";
  readonly style?: React.CSSProperties;
  readonly children: React.ReactNode;
}> = ({ block, swing, exit = true, align = "left", style, children }) => (
  <Phrase
    at={block.at}
    dur={block.dur}
    exitAt={exit ? block.exitAt : undefined}
    exitDur={block.exitDur}
    swing={swing}
    style={{ position: "absolute", left: block.box[0], top: block.box[1], width: block.box[2], textAlign: align, ...style }}
  >
    {children}
  </Phrase>
);

// The same placement, revealed word by word on `times` (the first is the block's `at`).
export const WordBlock: React.FC<{
  readonly block: TextBlock;
  readonly text: string;
  readonly times: ReadonlyArray<number>;
  readonly exit?: boolean;
  readonly align?: "left" | "center";
  readonly wordStyle?: Readonly<Record<number, React.CSSProperties>>;
  readonly lineStyle?: Readonly<Record<number, React.CSSProperties>>;
  readonly style?: React.CSSProperties;
}> = ({ block, text, times, exit = true, align = "left", wordStyle, lineStyle, style }) => (
  <Words
    text={text}
    times={times}
    exitAt={exit ? block.exitAt : undefined}
    exitDur={block.exitDur}
    wordStyle={wordStyle}
    lineStyle={lineStyle}
    style={{ position: "absolute", left: block.box[0], top: block.box[1], width: block.box[2], textAlign: align, ...style }}
  />
);
