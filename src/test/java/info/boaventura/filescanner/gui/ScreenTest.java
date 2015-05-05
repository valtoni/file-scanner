package info.boaventura.filescanner.gui;

import static org.junit.Assert.*;
import info.boaventura.filescanner.gui.Screen;

import org.junit.Test;

public class ScreenTest extends Screen {

  @Test
  public void test() {
    Screen sc = new Screen();
    assertEquals("0/0 (0,00%)", sc.messageStatus(0.0, 0.0));
    assertEquals("0/10 (0,00%)", sc.messageStatus(0.0, 10.0));
    assertEquals("5/10 (50,00%)", sc.messageStatus(5.0, 10.0));
    assertEquals("10/10 (100,00%)", sc.messageStatus(10.0, 10.0));
  }

}
