# ComposeFlow WASM Application

This module contains the WebAssembly (WASM) version of ComposeFlow.

> ⚠️ **EXPERIMENTAL**: The WASM version of ComposeFlow is still highly experimental. It may have bugs, performance issues, or missing features.

## Running the WASM App

To run the WASM version of ComposeFlow:

```bash
./gradlew :wasmApp:wasmJsBrowserDevelopmentRun
```

This will start a development server and open the app in your browser.

## Building for Production

To build the WASM app for production:

```bash
./gradlew :wasmApp:wasmJsBrowserProductionWebpack
```

The output will be in `wasmApp/build/dist/wasmJs/productionExecutable/`.

## Limitations

The WASM version has the following limitations compared to the desktop version:

- No software update functionality (Conveyor)
- No native file system access (projects are saved in browser's localStorage)
- No OAuth authentication (uses anonymous mode)
- No billing/pricing features
- No Jewel UI components (uses Material3)
- Limited clipboard functionality
- No native window management

Note: Projects are saved locally in the browser's localStorage. This means projects will persist between sessions but are limited to the specific browser and domain.

## Architecture

The WASM app shares most of the codebase with the desktop version through Kotlin Multiplatform:
- Common UI components from `:feature:*` modules
- Platform-specific implementations using expect/actual pattern
- WASM-specific implementations in `wasmJsMain` source sets
