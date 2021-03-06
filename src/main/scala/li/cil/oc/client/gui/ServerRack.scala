package li.cil.oc.client.gui

import java.util

import li.cil.oc.Localization
import li.cil.oc.Settings
import li.cil.oc.client.Textures
import li.cil.oc.client.{PacketSender => ClientPacketSender}
import li.cil.oc.common.container
import li.cil.oc.common.tileentity
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.Tessellator
import net.minecraft.entity.player.InventoryPlayer
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11

import scala.collection.convert.WrapAsJava._

class ServerRack(playerInventory: InventoryPlayer, val rack: tileentity.ServerRack) extends DynamicGuiContainer(new container.ServerRack(playerInventory, rack)) {
  protected var switchButton: ImageButton = _

  protected var powerButtons = new Array[ImageButton](4)

  protected var sideButtons = new Array[GuiButton](4)

  protected var rangeButtons = new Array[GuiButton](2)

  def sideName(number: Int) = rack.sides(number) match {
    case Some(ForgeDirection.UP) => Localization.ServerRack.Top
    case Some(ForgeDirection.DOWN) => Localization.ServerRack.Bottom
    case Some(ForgeDirection.EAST) => Localization.ServerRack.Left
    case Some(ForgeDirection.WEST) => Localization.ServerRack.Right
    case Some(ForgeDirection.NORTH) => Localization.ServerRack.Back
    case _ => Localization.ServerRack.None
  }

  def add[T](list: util.List[T], value: Any) = list.add(value.asInstanceOf[T])

  protected override def actionPerformed(button: GuiButton) {
    if (button.id >= 0 && button.id <= 3) {
      ClientPacketSender.sendServerPower(rack, button.id, !rack.isRunning(button.id))
    }
    if (button.id >= 4 && button.id <= 7) {
      val number = button.id - 4
      val sides = ForgeDirection.VALID_DIRECTIONS.map(Option(_)) ++ Seq(None)
      val currentSide = sides.indexOf(rack.sides(number))
      val searchSides = sides.drop(currentSide + 1) ++ sides.take(currentSide + 1)
      val nextSide = searchSides.find(side => side != Option(ForgeDirection.SOUTH) && (!rack.sides.contains(side) || side == None)) match {
        case Some(side) => side
        case _ => None
      }
      ClientPacketSender.sendServerSide(rack, number, nextSide)
    }
    if (button.id >= 8 && button.id <= 9) {
      val step =
        if (GuiScreen.isShiftKeyDown) 32
        else if (GuiScreen.isCtrlKeyDown) 1
        else 8
      val range =
        if (button.id == 8) math.max(rack.range - step, 0)
        else math.min(rack.range + step, Settings.get.maxWirelessRange.toInt)
      if (range != rack.range) {
        ClientPacketSender.sendServerRange(rack, range)
      }
    }
    if (button.id == 10) {
      ClientPacketSender.sendServerSwitchMode(rack, !rack.internalSwitch)
    }
  }

  override def drawScreen(mouseX: Int, mouseY: Int, dt: Float) {
    for (i <- 0 to 3) {
      powerButtons(i).toggled = rack.isRunning(i)
      sideButtons(i).displayString = sideName(i)
    }
    switchButton.displayString = if (rack.internalSwitch) Localization.ServerRack.SwitchInternal else Localization.ServerRack.SwitchExternal
    super.drawScreen(mouseX, mouseY, dt)
  }

  override def initGui() {
    super.initGui()
    for (i <- 0 to 3) {
      powerButtons(i) = new ImageButton(i, guiLeft + 84, guiTop + 7 + i * 18, 18, 18, Textures.guiButtonPower, canToggle = true)
      add(buttonList, powerButtons(i))
    }
    for (i <- 0 to 3) {
      sideButtons(i) = new ImageButton(4 + i, guiLeft + 126, guiTop + 7 + i * 18, 42, 18, Textures.guiButtonSide, sideName(i))
      add(buttonList, sideButtons(i))
    }
    for (i <- 0 to 1) {
      rangeButtons(i) = new ImageButton(8 + i, guiLeft + 8 + i * 48, guiTop + 50, 16, 18, Textures.guiButtonRange, if (i == 0) "-" else "+")
      add(buttonList, rangeButtons(i))
    }
    switchButton = new ImageButton(10, guiLeft + 8, guiTop + 17, 64, 18, Textures.guiButtonSwitch, Localization.ServerRack.SwitchExternal, textIndent = 18)
    add(buttonList, switchButton)
  }

  override def drawSecondaryForegroundLayer(mouseX: Int, mouseY: Int) = {
    super.drawSecondaryForegroundLayer(mouseX, mouseY)
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS) // Prevents NEI render glitch.

    fontRendererObj.drawString(
      Localization.localizeImmediately(rack.getInventoryName),
      8, 6, 0x404040)

    val rangeY = 39
    fontRendererObj.drawString(Localization.ServerRack.WirelessRange, 8, rangeY, 0x404040)

    {
      // Background for range value.
      val tx = 25
      val ty = 50
      val w = 30
      val h = 18
      val t = Tessellator.instance
      mc.getTextureManager.bindTexture(Textures.guiRange)
      GL11.glColor3f(1, 1, 1)
      GL11.glDepthMask(false)
      t.startDrawingQuads()
      t.addVertexWithUV(tx, ty + h, zLevel, 0, 1)
      t.addVertexWithUV(tx + w, ty + h, zLevel, 1, 1)
      t.addVertexWithUV(tx + w, ty, zLevel, 1, 0)
      t.addVertexWithUV(tx, ty, zLevel, 0, 0)
      t.draw()
      GL11.glDepthMask(true)
    }

    drawCenteredString(fontRendererObj,
      rack.range.toString,
      40, 56, 0xFFFFFF)

    for (i <- 0 to 3 if powerButtons(i).func_146115_a) {
      val tooltip = new java.util.ArrayList[String]
      tooltip.addAll(asJavaCollection(if (rack.isRunning(i)) Localization.Computer.TurnOff.lines.toIterable else Localization.Computer.TurnOn.lines.toIterable))
      copiedDrawHoveringText(tooltip, mouseX - guiLeft, mouseY - guiTop, fontRendererObj)
    }

    GL11.glPopAttrib()
  }
}
