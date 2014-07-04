package pHX_2;

import static repast.simphony.essentials.RepastEssentials.GetParameter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunInfo;
import repast.simphony.engine.environment.RunState;
import repast.simphony.parameter.Schema;

public class SQLDataCollector {

	private static int simID;
	private static Connection conn = null;
	private Context<Object> context = null;
	private RunInfo runInfo;
	private PreparedStatement mktDataPstm;
	private PreparedStatement firmsConstDataPstm;
	private PreparedStatement firmsPerTickDataPstm;
	
	public SQLDataCollector(Context<Object> context) {
		super();
		this.context = context;

		context.add(this);

		runInfo = RunState.getInstance().getRunInfo();

		// Check if the first run
		if (conn == null) {

			// Creates the connection to database server
			try {
				conn = createCon();
				System.out.println("Connection to database established");
			} catch (SQLException e) {
				e.printStackTrace();
				System.err
				.println("Connection to database server could not be established");
				System.exit(-1);
			}

			// Gets the next simID from database
			simID = nextSimID();

			saveSimInfo();

		}

		saveRunParams();

		/*
		 * This is redundat, it will replace saveRunParams and part of
		 * saveSimInfo (param info) The idea is to split params that depend on
		 * run from the ones that depend on Sim, later using sql. This will
		 * provide more flexibility for robustness check
		 */
		saveAllParams();

		createPrepStatments();

	}

	private Connection createCon() throws SQLException {

		/*
		 * The class loader used when the GUI calls the batch doesn't find the
		 * sql driver. Nick Nicollier suggested using another class loader and
		 * it worked
		 */
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(
				Context.class.getClassLoader());

		// Creates the connection to SQL Server
		String sqlSrv = (String) GetParameter("SQLServer");
		String db = (String) GetParameter("database");
		String conStr = "jdbc:sqlserver://" + sqlSrv + ";databaseName=" + db
				+ ";integratedSecurity=true;";

		conn = DriverManager.getConnection(conStr);

		// Part of Nick suggestion
		Thread.currentThread().setContextClassLoader(current);

		return conn;

	}

	private static Integer nextSimID() {
		// Get next simulation number
		String sqlStr = "SELECT MAX(SimID) FROM Simulations";

		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sqlStr);
			Integer s;
			if (rs.next()) {
				s = rs.getInt(1) + 1;
			} else {
				s = 1;
			}
			return s;

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (Throwable ignore) {
			}
		}

		return null;
	}

	/*
	 * Returns Simulation ID number
	 */
	private void saveSimInfo() {

		// Saves simulation data and parameters of the whole simulation
		saveSimulationData();
		saveSimulationParams();

		System.out.println("Simulation Info saved");

	}

	private static void saveSimulationData() {
		String desc = (String) GetParameter("simDescription");

		String sqlStr = "INSERT INTO Simulations (SimID, Description) "
				+ "VALUES (" + simID + ", '" + desc + "' )";

		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(sqlStr);
			System.out.println("Simulation " + simID
					+ " added to Table \"Simulations\".");

		} catch (SQLException e) {
			e.printStackTrace();
			System.err
			.println("Error trying to write to \"Simulations\" table.");
			System.exit(-1);
		} finally {
			try {
				stmt.close();
			} catch (Throwable ignore) {
			}
		}

	}

	private static void saveSimulationParams() {

		String sqlStr = "INSERT INTO SimulationParameters VALUES (" + simID
				+ ", ? , ? )";

		Schema schema = RunEnvironment.getInstance().getParameters()
				.getSchema();

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sqlStr);
			for (String paramName : schema.parameterNames()) {
				if (isSimData(paramName) || isRunParam(paramName)) {
					continue;
				} else {
					pstmt.setString(1, paramName);
					pstmt.setString(2, GetParameter(paramName).toString());
					pstmt.executeUpdate();
				}
			}
			System.out
			.println("Parameters of the whole simulation saved to table \"Simulation Parameters\".");

		} catch (SQLException e) {
			e.printStackTrace();
			System.err
			.println("Error trying to write to \"Simulation Parameters\" table.");
			System.exit(-1);
		} finally {
			try {
				pstmt.close();
			} catch (Throwable ignore) {
			}
		}

	}

	private void saveAllParams() {
		int run = RunState.getInstance().getRunInfo().getRunNumber();

		String sqlStr = "INSERT INTO AllParameters VALUES (" + simID + ", "
				+ run + ", ?, ? )";

		Schema schema = RunEnvironment.getInstance().getParameters()
				.getSchema();

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sqlStr);
			for (String paramName : schema.parameterNames()) {
				if (isSimData(paramName)) {
					continue;
				} else {
					pstmt.setString(1, paramName);
					pstmt.setString(2, GetParameter(paramName).toString());
					pstmt.executeUpdate();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		} finally {
			try {
				pstmt.close();
			} catch (Throwable ignore) {
			}
		}

	}

	private void saveRunParams() {
		int run = RunState.getInstance().getRunInfo().getRunNumber();
		// double ssM = (Double) GetParameter("suddenStopMagnitude");
		// int ssS = (Integer) GetParameter("suddenStopStart");
		int rndSeed = (Integer) GetParameter("randomSeed");

		String sqlStr = "";
		/*
		 * String sqlStr = "INSERT INTO RunParameters VALUES (" + simID + ", " +
		 * run + ", " + ssM + "," + ssS + ", " + rndSeed + " )";
		 */

		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(sqlStr);

		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		} finally {
			try {
				stmt.close();
			} catch (Throwable ignore) {
			}
		}

		System.out.println("Run: " + run + " started");

	}

	private void createPrepStatments() {
		String mktDataStr = "INSERT INTO MarketData "
				+ "( Simulation, RunNumber, Tick, Price, TotalQuantity ) "
				+ "VALUES ( ?, ? , ?, ?, ? )";

		String firmsConstDataStr = "INSERT INTO IndividualFirms ("
				+ "Simulation, RunNumber, Firm, "
				+ "InitialFUC, RDEfficiency, TargetLeverage, "
				+ "LearningRate, Born ) " + "VALUES (?,?,?,?,?,?,?,?)";


		String firmsPerTickDataStr = "INSERT INTO [IndividualFirmsPerTick] ("
				+ "Simulation, RunNumber, Tick, Firm, "
				+ "Profit, Quantity, Capital, Debt, "
				+ "AcumQ, AcumProfit, MedCost, MktShare, Interest ) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";


		
		try {
			mktDataPstm = conn.prepareStatement(mktDataStr);
			firmsConstDataPstm = conn.prepareStatement(firmsConstDataStr);
			firmsPerTickDataPstm = conn.prepareStatement(firmsPerTickDataStr);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);

		}
		 
	}

	private static boolean isSimData(String paramName) {
		return (paramName == "simDescription");
	}

	private static boolean isRunParam(String paramName) {
		return (paramName == "suddenStopMagnitude"
				|| paramName == "suddenStopStart" || paramName == "randomSeed");
	}
}
