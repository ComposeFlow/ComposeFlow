package io.composeflow.device

sealed class Device(
    val deviceName: String,
) {
    data class AndroidEmulator(
        /**
         * The name of the device obtained by `adb devices` command.
         */
        val name: String,
        val status: EmulatorStatus = EmulatorStatus.Offline,
    ) : Device(deviceName = name) {
        fun adbTarget() = "emulator-${status.portNumber}"
    }

    data class IosSimulator(
        val name: String,
        val deviceId: String,
        val status: SimulatorStatus,
    ) : Device(deviceName = name)

    data object Web : Device("Web")
}

enum class EmulatorStatus(
    val displayName: String,
) {
    Offline("offline"),
    Device("device"),
    NoDevice("no device"),
    ;

    var portNumber: Int = 0

    companion object {
        fun fromString(string: String) =
            when (string) {
                Offline.displayName -> Offline
                Device.displayName -> Device
                else -> NoDevice
            }
    }
}

enum class SimulatorStatus(
    val displayName: String,
) {
    Booted("Booted"),
    Shutdown("Shutdown"),
    Unavailable("Unavailable"),
    ;

    companion object {
        fun fromString(string: String) =
            when (string) {
                Booted.displayName -> Booted
                Shutdown.displayName -> Shutdown
                else -> Unavailable
            }
    }
}

fun List<Device>.nextPortNumber(): Int {
    val onlineEmulators =
        filter { it is Device.AndroidEmulator && it.status == EmulatorStatus.Device }
    return if (onlineEmulators.isNotEmpty()) {
        onlineEmulators.maxOf { (it as Device.AndroidEmulator).status.portNumber } + 2
    } else {
        5554
    }
}
