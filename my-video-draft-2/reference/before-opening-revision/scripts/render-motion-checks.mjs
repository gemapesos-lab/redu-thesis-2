import { bundle } from "@remotion/bundler";
import { enableTailwind } from "@remotion/tailwind-v4";
import { getCompositions, openBrowser, renderStill } from "@remotion/renderer";
import { mkdirSync } from "node:fs";
import { resolve } from "node:path";

// Render inside AVP: isolated scene stills did not reveal the compositing fault.
mkdirSync("out/motion-qa", { recursive: true });
const serveUrl = await bundle({ entryPoint: resolve("src/index.ts"), outDir: resolve("out/.motion-check-bundle"), rspack: true, bundlerOverride: enableTailwind });
const browser = await openBrowser("chrome");
try {
  const composition = (await getCompositions(serveUrl, { puppeteerInstance: browser })).find(c => c.id === "AVP");
  for (const frame of [107, 720, 858, 866, 874]) {
    const output = `out/motion-qa/composite-${frame}.png`;
    await renderStill({ serveUrl, composition, puppeteerInstance: browser, imageFormat: "png", output, frame });
    console.log(output);
  }
} finally {
  await browser.close({ silent: true });
}
