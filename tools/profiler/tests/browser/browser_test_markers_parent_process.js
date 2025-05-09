/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

add_task(async function test_markers_parent_process() {
  info("Test markers that are generated by the browser's parent process.");

  info("Start the profiler in nostacksampling mode.");
  await ProfilerTestUtils.startProfiler({ features: ["nostacksampling"] });

  info("Dispatch a DOMEvent");
  window.dispatchEvent(new Event("synthetic"));

  info("Stop the profiler and get the profile.");
  const profile = await ProfilerTestUtils.stopNowAndGetProfile();

  const markers = ProfilerTestUtils.getInflatedMarkerData(profile.threads[0]);
  {
    const domEventStart = markers.find(
      ({ phase, data }) =>
        phase === ProfilerTestUtils.markerPhases.INTERVAL_START &&
        data?.eventType === "synthetic"
    );
    const domEventEnd = markers.find(
      ({ phase, data }) =>
        phase === ProfilerTestUtils.markerPhases.INTERVAL_END &&
        data?.eventType === "synthetic"
    );
    ok(domEventStart, "A start DOMEvent was generated");
    ok(domEventEnd, "An end DOMEvent was generated");
    Assert.greater(
      domEventEnd.data.latency,
      0,
      "DOMEvent had a a latency value generated."
    );
    Assert.strictEqual(domEventEnd.data.type, "DOMEvent");
    Assert.strictEqual(domEventEnd.name, "DOMEvent");
  }
  // Add more marker tests.
});
