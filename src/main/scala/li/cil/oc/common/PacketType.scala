package li.cil.oc.common

object PacketType extends Enumeration {
  val
  // Server -> Client
  AbstractBusState,
  Analyze,
  ChargerState,
  ComputerState,
  ComputerUserList,
  HologramClear,
  HologramPowerChange,
  HologramScale,
  HologramSet,
  PowerState,
  RedstoneState,
  RobotAnimateSwing,
  RobotAnimateTurn,
  RobotEquippedItemChange,
  RobotEquippedUpgradeChange,
  RobotMove,
  RobotSelectedSlotChange,
  RobotXp,
  RotatableState,
  ScreenColorChange,
  ScreenCopy,
  ScreenDepthChange,
  ScreenFill,
  ScreenPowerChange,
  ScreenResolutionChange,
  ScreenSet,
  ServerPresence,

  // Client -> Server
  ComputerPower,
  KeyDown,
  KeyUp,
  Clipboard,
  MouseClickOrDrag,
  MouseScroll,
  MultiPartPlace,
  RobotStateRequest,
  ServerSide,
  ServerRange = Value
}