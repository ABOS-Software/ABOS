import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.JMapViewerTree;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Map extends JFrame implements JMapViewerEventListener {
    // --Commented out by Inspection (1/2/2016 12:01 PM):private static final long serialVersionUID = 1L;
    public JLabel Address = new JLabel("");
    public JLabel OrderStat = new JLabel("");
    public JLabel name = new JLabel("");
    public JLabel Phone = new JLabel("");
    public Object[] cPoints;
    public JPanel infoPanel = new JPanel();
    private JLabel Orders = new JLabel("");
    //TODO FIx zoom on map markers and colors
    private JMapViewerTree treeMap = null;
    private JLabel zoomLabel = null;
    private JLabel zoomValue = null;
    private JLabel mperpLabelName = null;
    private JLabel mperpLabelValue = null;
    // --Commented out by Inspection (1/2/2016 12:01 PM):private JFrame frame;

    /**
     * Launch the application.
     */

	/*public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Map window = new Map();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}*/
    public Map() {
        super("JMapViewer Map");

        setSize(600, 400);
        treeMap = new JMapViewerTree("Zones");
        map().addJMVListener(this);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(3);
        setExtendedState(6);
        JPanel panel = new JPanel();
        JPanel panelTop = new JPanel();
        JPanel panelBottom = new JPanel();
        JPanel helpPanel = new JPanel();


        mperpLabelName = new JLabel("Meters/Pixels: ");
        mperpLabelValue = new JLabel(String.format("%s", new Object[]{Double.valueOf(map().getMeterPerPixel())}));
        zoomLabel = new JLabel("Zoom: ");
        zoomValue = new JLabel(String.format("%s", new Object[]{Integer.valueOf(map().getZoom())}));
        add(panel, "North");
        add(helpPanel, "South");
        panel.setLayout(new BorderLayout());
        panel.add(panelTop, "North");
        panel.add(panelBottom, "South");
        JLabel helpLabel = new JLabel("Use right mouse button to move,\n left double click or mouse wheel to zoom.");
        helpPanel.add(helpLabel);
        JLabel AddressL = new JLabel("Address:");
        JLabel OrderStatL = new JLabel("Order Status:");
        JLabel nameL = new JLabel("Name:");
        JLabel PhoneL = new JLabel("Phone:");
        JLabel OrdersL = new JLabel("Orders:");


        infoPanel.add(AddressL);
        infoPanel.add(Address);
        Address.setBorder(new EmptyBorder(0, 0, 15, 0));
        infoPanel.add(OrderStatL);
        infoPanel.add(OrderStat);
        OrderStat.setBorder(new EmptyBorder(0, 0, 15, 0));
        infoPanel.add(nameL);
        infoPanel.add(name);
        name.setBorder(new EmptyBorder(0, 0, 15, 0));
        infoPanel.add(PhoneL);
        infoPanel.add(Phone);
        Phone.setBorder(new EmptyBorder(0, 0, 15, 0));
        infoPanel.add(OrdersL);
        infoPanel.add(Orders);
        Orders.setBorder(new EmptyBorder(0, 0, 15, 0));


        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.PAGE_AXIS));
        //infoPanel.setLayout(new GridLayout(10, 1, 3, 50));
        //infoPanel.setLayout(new FlowLayout());
        add(infoPanel, "East");

        map().setTileSource(new OsmTileSource.Mapnik());
        map().setTileLoader(new OsmTileLoader(map()));


        map().setMapMarkerVisible(true);


        panelTop.add(zoomLabel);
        panelTop.add(zoomValue);
        panelTop.add(mperpLabelName);
        panelTop.add(mperpLabelValue);
        add(treeMap, "Center");

        //Add customers to map
        List<String> Addr = getCustInfo("ADDRESS");
        List<String> Ord = getCustInfo("Ordered");
        List<String> NI = getCustInfo("NI");
        java.util.List<String> NH = getCustInfo("NH");
        cPoints = new Object[Addr.size()];
        for (int i = 0; i < Addr.size(); i++) {
            try {
                Object[][] coords = GetCoords(Addr.get(i));
                double lat = Double.valueOf(coords[0][0].toString());
                double lon = Double.valueOf(coords[0][1].toString());
                MapMarkerDot m = new MapMarkerDot(lat, lon);
                cPoints[i] = new cPoint(lat, lon, Addr.get(i));
                //Determine color of dot
                //Green = orderd
                //Cyan = Not Interested
                //Magenta = not home
                if (Ord.get(Ord.size() - 1).equals("True")) {
                    m.setBackColor(Color.GREEN);
                }
                if (NI.get(NI.size() - 1).equals("True")) {
                    m.setBackColor(Color.CYAN);
                }
                if (NH.get(NH.size() - 1).equals("True")) {
                    m.setBackColor(Color.MAGENTA);
                }
                map().addMapMarker(m);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        new MapController(map(), this);
        map().setDisplayToFitMapMarkers();

    }

// --Commented out by Inspection START (1/2/2016 12:01 PM):
//    private static Coordinate c(double lat, double lon) {
//        return new Coordinate(lat, lon);
//    }
// --Commented out by Inspection STOP (1/2/2016 12:01 PM)

    public static void main(String... args) {
        Map window = new Map();
        window.setVisible(true);

    }

// --Commented out by Inspection START (1/2/2016 12:01 PM):
//    /**
//     * Create the application.
//     */
///*	public Map() {
//        initialize();
//		try {
////			Object[][] coords = GetCoords("***REMOVED***");
////			System.out.println(coords[0][0]);
////			System.out.println(coords[0][1]);
//		}
//			catch(java.lang.Exception e) {
//					System.out.println(e.toString());
//			}
//		}*/
//    public Object[] getCPoints() {
//        return cPoints;
//    }
// --Commented out by Inspection STOP (1/2/2016 12:01 PM)

    /**
     * Getsx the requested info on customers
     *
     * @param info Info to retrieve
     * @return The info requested in ArrayList form
     */
    private List<String> getCustInfo(String info) {
        List<String> ret = new ArrayList<String>();

        try (PreparedStatement prep = DbInt.getPrep("Set", "SELECT * FROM Customers");
             ResultSet rs = prep.executeQuery()
        ) {


            while (rs.next()) {

                ret.add(rs.getString(info));

            }
            ////DbInt.pCon.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private JMapViewer map() {
        return treeMap.getViewer();
    }

    private void updateZoomParameters() {
        if (mperpLabelValue != null) {
            mperpLabelValue.setText(String.format("%s", Double.valueOf(map().getMeterPerPixel())));
        }

        if (zoomValue != null) {
            zoomValue.setText(String.format("%s", Integer.valueOf(map().getZoom())));
        }

    }

    @Override
    public void processCommand(JMVCommandEvent command) {
        if ((command.getCommand() == JMVCommandEvent.COMMAND.ZOOM) || (command.getCommand() == JMVCommandEvent.COMMAND.MOVE)) {
            updateZoomParameters();
        }

    }


    /**Gets coords of an address
     * @param Address Address to get coords of
     * @return Object[][] that holds the houses coordinates
     * @throws IOException
     */
    private Object[][] GetCoords(String Address) throws IOException {
        String AddressF = Address.replace(" ", "+");
        String url = String.format("http://open.mapquestapi.com/nominatim/v1/search.php?key=CCBtW1293lbtbxpRSnImGBoQopnvc4Mz&format=xml&q=%s&addressdetails=0&limit=1", AddressF);

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        String USER_AGENT = "Mozilla/5.0";
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }


            //print result
            return parseCoords(response.toString());
        }
    }

    /**Gets coords of an address
     * @param xml THe XML to parse
     * @return Object[][] that holds the houses coordinates
     */
    private Object[][] parseCoords(String xml) {
        Object[][] coords = new Object[1][2];
        try {
            InputSource is = new InputSource(new StringReader(xml));

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

            NodeList nList = doc.getElementsByTagName("place");


            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);


                if ((int) nNode.getNodeType() == (int) Node.ELEMENT_NODE) {



                    coords[0][0] = ((Element) nNode).getAttributeNode("lat").getValue();
                    coords[0][1] = ((Element) nNode).getAttributeNode("lon").getValue();


                    //final Object[] columnNames = {"Product Name", "Size", "Price/Item", "Quantity", "Total Cost"};


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return coords;
    }
    /*
      Initialize the contents of the frame.
     */
/*	private void initialize() {
        frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}*/

}
