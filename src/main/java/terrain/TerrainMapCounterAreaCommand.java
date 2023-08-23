package terrain;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.MassKeyCommand;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.configure.BooleanConfigurer;
import VASSAL.configure.ChooseComponentDialog;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.configure.PropertyExpression;
import VASSAL.configure.PropertyExpressionConfigurer;
import VASSAL.configure.StringConfigurer;
import VASSAL.counters.BooleanAndPieceFilter;
import VASSAL.counters.Decorator;
import VASSAL.counters.GamePiece;
import VASSAL.counters.GlobalCommand;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.PieceEditor;
import VASSAL.counters.PieceFilter;
import VASSAL.i18n.TranslatablePiece;
import VASSAL.tools.RecursionLimiter.Loopable;
import VASSAL.tools.SequenceEncoder;

/**
 * 
 * @author morvael
 */
public class TerrainMapCounterAreaCommand extends Decorator implements TranslatablePiece, PieceFilter, Loopable {

  public static final String ID = "TerrainMapCounterAreaCommand;";
  protected KeyCommand[] commands;
  protected String commandText;
  protected KeyStroke commandKey;
  private KeyCommand command;
  protected PropertyExpression propertiesFilter = new PropertyExpression();
  protected GlobalCommand globalCommand = new GlobalCommand(this);
  protected String terrainMapShaderName;
  protected TerrainMapShader shader;
  protected Set<HexRef> shadedHexes;
  protected String description = "";

  public TerrainMapCounterAreaCommand() {
    this(ID, null);
  }

  public TerrainMapCounterAreaCommand(String type, GamePiece inner) {
    mySetType(type);
    setInner(inner);
  }

  public void mySetType(String type) {
    type = type.substring(ID.length());
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(type, ';');
    commandText = st.nextToken("");
    commandKey = st.nextKeyStroke(null);
    command = new KeyCommand(commandText, commandKey, Decorator.getOutermost(this), this);
    propertiesFilter.setExpression(st.nextToken(""));
    globalCommand.setKeyStroke(st.nextKeyStroke(null));
    globalCommand.setReportSingle(st.nextBoolean(true));
    globalCommand.setSelectFromDeck(st.nextInt(-1));
    terrainMapShaderName = st.nextToken("");
    if (terrainMapShaderName.length() == 0) {
      terrainMapShaderName = null;
    }
    description = st.nextToken("");
    shader = null;
    commands = null;
  }

  public String myGetType() {
    SequenceEncoder se = new SequenceEncoder(';');
    se.append(commandText);
    se.append(commandKey);
    se.append(propertiesFilter.getExpression());
    se.append(globalCommand.getKeyStroke());
    se.append(globalCommand.isReportSingle());
    se.append(globalCommand.getSelectFromDeck());
    se.append(terrainMapShaderName == null ? "" : terrainMapShaderName);
    se.append(description);
    return ID + se.getValue();
  }

  protected TerrainMapShader getShader() {
    if (shader == null) {
      for (TerrainMapShader sh : piece.getMap().getAllDescendantComponentsOf(TerrainMapShader.class)) {
        if (sh.getConfigureName().equals(terrainMapShaderName)) {
          shader = sh;
          return shader;
        }
      }
      return null;
    } else {
      return shader;
    }
  }

  protected KeyCommand[] myGetKeyCommands() {
    if (commands == null) {
      if (commandText.length() == 0) {
        commands = new KeyCommand[0];
      } else {
        commands = new KeyCommand[]{command};
      }
    }
    return commands;
  }

  public String myGetState() {
    return "";
  }

  public Command myKeyEvent(KeyStroke stroke) {
    Command c = null;
    myGetKeyCommands();
    if (command.matches(stroke)) {
      apply();
    }
    return c;
  }

  public void mySetState(String newState) {
  }

  public Rectangle boundingBox() {
    return piece.boundingBox();
  }

  public void draw(Graphics g, int x, int y, Component obs, double zoom) {
    piece.draw(g, x, y, obs, zoom);
  }

  public String getName() {
    return piece.getName();
  }

  public Shape getShape() {
    return piece.getShape();
  }

  public PieceEditor getEditor() {
    return new Ed(this);
  }

  public String getDescription() {
    String d = "Terrain Map Counter Area Command";
    if (description.length() > 0) {
      d += " - " + description;
    }
    return d;
  }

  public HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("TerrainMapCounterAreaCommand.htm");
  }

  @Override
  public boolean accept(GamePiece piece) {
    return shadedHexes.contains(shader.getGrid().getHexPos(piece.getPosition()));
  }

  public void apply() {
    getShader();
    if (shader != null) {
      shadedHexes = shader.getHexes(shader.getRules(), Decorator.getOutermost(this)).keySet();
      PieceFilter filter = propertiesFilter.getFilter(Decorator.getOutermost(this));
      Command c = new NullCommand();
      filter = new BooleanAndPieceFilter(filter, this);
      for (Map m : Map.getMapList()) {
        c = c.append(globalCommand.apply(m, filter));
      }
      GameModule.getGameModule().sendAndLog(c);
    }
  }

  public static class Ed implements PieceEditor {

    protected JPanel panel;
    protected StringConfigurer descriptionConfigurer;
    protected Box selectShader;
    protected String mapShaderId;
    protected StringConfigurer commandTextConfigurer;
    protected HotKeyConfigurer commandKeyConfigurer;
    protected StringConfigurer propertiesFilterConfigurer;
    protected HotKeyConfigurer globalCommandKeyStrokeConfigurer;
    protected BooleanConfigurer globalCommandReportSingleConfigurer;
    protected MassKeyCommand.DeckPolicyConfig globalCommandSelectFromDeckConfigurer;

    public Ed(TerrainMapCounterAreaCommand trait) {
      panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

      panel.add(new JLabel("Contributed by morvael", JLabel.CENTER));
      panel.add(new JSeparator());
      panel.add(new JLabel(" "));

      descriptionConfigurer = new StringConfigurer(null, "Description:  ", trait.description);
      panel.add(descriptionConfigurer.getControls());

      mapShaderId = trait.terrainMapShaderName;
      selectShader = Box.createHorizontalBox();

      panel.add(selectShader);
      JLabel l = new JLabel("Map Shading:  ");
      selectShader.add(l);
      final JTextField tf = new JTextField(12);
      tf.setEditable(false);
      selectShader.add(tf);
      tf.setText(trait.terrainMapShaderName);
      JButton b = new JButton("Select");
      selectShader.add(b);
      b.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          ChooseComponentDialog d = new ChooseComponentDialog((Frame) SwingUtilities.getAncestorOfClass(Frame.class, panel), TerrainMapShader.class);
          d.setVisible(true);
          if (d.getTarget() != null) {
            mapShaderId = d.getTarget().getConfigureName();
            tf.setText(mapShaderId);
          } else {
            mapShaderId = null;
            tf.setText("");
          }
        }
      });

      commandTextConfigurer = new StringConfigurer(null, "Command text:  ", trait.commandText);
      commandKeyConfigurer = new HotKeyConfigurer(null, "Command keyboard shortcut:  ", trait.commandKey);
      propertiesFilterConfigurer = new PropertyExpressionConfigurer(null, "Matching Properties:  ", trait.propertiesFilter);
      globalCommandKeyStrokeConfigurer = new HotKeyConfigurer(null, "Global Key Command:  ", trait.globalCommand.getKeyStroke());
      globalCommandReportSingleConfigurer = new BooleanConfigurer(null, "Suppress individual reports?", trait.globalCommand.isReportSingle());
      globalCommandSelectFromDeckConfigurer = new MassKeyCommand.DeckPolicyConfig();
      globalCommandSelectFromDeckConfigurer.setValue(new Integer(trait.globalCommand.getSelectFromDeck()));

      panel.add(commandTextConfigurer.getControls());
      panel.add(commandKeyConfigurer.getControls());
      panel.add(propertiesFilterConfigurer.getControls());
      panel.add(globalCommandKeyStrokeConfigurer.getControls());
      panel.add(globalCommandReportSingleConfigurer.getControls());
      panel.add(globalCommandSelectFromDeckConfigurer.getControls());
    }

    public Component getControls() {
      return panel;
    }

    public String getType() {
      SequenceEncoder se = new SequenceEncoder(';');
      se.append(commandTextConfigurer.getValueString());
      se.append((KeyStroke) commandKeyConfigurer.getValue());
      se.append(propertiesFilterConfigurer.getValueString());
      se.append((KeyStroke) globalCommandKeyStrokeConfigurer.getValue());
      se.append(globalCommandReportSingleConfigurer.booleanValue().booleanValue());
      se.append(globalCommandSelectFromDeckConfigurer.getIntValue());
      se.append(mapShaderId != null ? mapShaderId : "");
      se.append(descriptionConfigurer.getValueString());
      return TerrainMapCounterAreaCommand.ID + se.getValue();
    }

    public String getState() {
      return "";
    }
  }

  public String getComponentName() {
    return Decorator.getOutermost(this).getLocalizedName();
  }

  public String getComponentTypeName() {
    return getDescription();
  }
}
