package com.donglu.carpark.ui;

import java.awt.Canvas;
import java.awt.Frame;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.GridData;
import org.eclipse.wb.swt.SWTResourceManager;
import org.joda.time.DateTime;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

import antlr.ByteBuffer;

import com.donglu.carpark.model.CarparkMainModel;
import com.donglu.carpark.service.CarparkDatabaseServiceProvider;
import com.donglu.carpark.service.CarparkInOutServiceI;
import com.donglu.carpark.service.CarparkService;
import com.donglu.carpark.ui.common.AbstractApp;
import com.donglu.carpark.ui.common.App;
import com.donglu.carpark.ui.wizard.AddDeviceModel;
import com.donglu.carpark.ui.wizard.AddDeviceWizard;
import com.dongluhitec.card.common.ui.CommonUIFacility;
import com.dongluhitec.card.common.ui.uitl.JFaceUtil;
import com.dongluhitec.card.domain.LinkProtocolEnum;
import com.dongluhitec.card.domain.LinkTypeEnum;
import com.dongluhitec.card.domain.db.Device;
import com.dongluhitec.card.domain.db.Link;
import com.dongluhitec.card.domain.db.LinkStyleEnum;
import com.dongluhitec.card.domain.db.SerialDeviceAddress;
import com.dongluhitec.card.domain.db.singlecarpark.DeviceRoadTypeEnum;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkBlackUser;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkCarpark;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkDevice;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkInOutHistory;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkSystemSetting;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkUser;
import com.dongluhitec.card.domain.db.singlecarpark.SystemSettingTypeEnum;
import com.dongluhitec.card.domain.exception.DongluAppException;
import com.dongluhitec.card.domain.util.StrUtil;
import com.dongluhitec.card.hardware.device.WebCameraDevice;
import com.dongluhitec.card.hardware.service.BasicHardwareService;
import com.dongluhitec.card.hardware.xinluwei.XinlutongCallback.XinlutongResult;
import com.dongluhitec.card.hardware.xinluwei.XinlutongJNA;
import com.dongluhitec.card.server.servlet.ImageServlet;
import com.dongluhitec.card.service.util.ImageUtil;
import com.dongluhitec.card.ui.carpark.pay.storein.wizard.NewCarparkStoreInHistoryModel;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.events.KeyAdapter;

public class CarparkMainApp extends AbstractApp implements XinlutongResult {
	private static final String VILIDTO_DATE = ",有效期至yyyy年MM月dd日";

	public static final String IMAGE_SAVE_SITE = "imageSaveSite";//图片保存位置

	private static final String USUAL_MSG = "欢迎光临";

	private static final String TEMP_CAR_AUTO_PASS = "tempCarAutoPass";

	private static final String CAR_IS_ARREARS = "车辆已到期,请联系管理员";

	private static final String CAR_OUT_MSG = "祝您一路平安";

	private static final String CAR_IN_MSG = "欢迎光临,请入场停车";

	private static final String NOT_PERMIT_TEMPCAR_IN_MSG = "固定停车场，不容许临时车进入";

	private static final String TEMP_ROAD = "临时车通道";

	private static final String FIX_ROAD = "固定车通道";

	protected static final String CAR_WILL_ARREARS = "车辆即将到期";

	private DataBindingContext m_bindingContext;

	private Logger LOGGER = LoggerFactory.getLogger(CarparkMainApp.class);

	protected Shell shell;
	private Text text_total;
	private Text text_hours;
	private Text text_month;
	private Text txt_userName;
	private Text text_worTime;
	private Text text_charge;
	private Text text_free;
	private Text text_carName;
	private Text txt_plateNO;
	private Text text_carType;
	private Text text_intime;
	private Text text_outTime;
	private Text text_totalTime;
	private Text text_should;
	private Text text_real;
	private Text txtinplateNo;
	private Text text_in_time;
	private Text txtoutplateNo;
	private Text text_out_time;

	@Inject
	private XinlutongJNA xinlutongJNA;
	@Inject
	private CommonUIFacility commonui;
	@Inject
	private CarparkMainPresenter presenter;
	@Inject
	private BasicHardwareService hardwareService;
	@Inject
	private CarparkDatabaseServiceProvider sp;

	private CarparkMainModel model;

	private CLabel inBigImg;
	private CLabel inSmallImg;
	private CLabel outSmallImg;
	private CLabel outBigImg;

	private Image inSmallImage;
	private Image inBigImage;
	private Image outSmallImage;
	private Image outBigImage;

	AtomicInteger plateNoTotal = new AtomicInteger(0);

	// 保存设备的进出口信息
	Map<String, String> mapDeviceType = Maps.newHashMap();

	// 保存设备的界面信息
	Map<CTabItem, String> mapDeviceTabItem = Maps.newHashMap();
	// 保存设备的信息
	Map<String, SingleCarparkDevice> mapIpToDevice = Maps.newHashMap();
	// 保存设置信息
	private Map<SystemSettingTypeEnum, String> mapSystemSetting = Maps.newHashMap();
	// 保存黑名单信息
	Map<String, SingleCarparkBlackUser> mapBlackUser = Maps.newHashMap();
	// 进口tab
	private CTabFolder tabInFolder;
	// 出口tab
	private CTabFolder tabOutFolder;

	private String userType;
	private Label lblNewLabel;
	private Button btnNewButton;
	private Button btnf;
	// 保存最近的手动拍照时间
	private Map<String, Date> mapHandPhotograph = Maps.newHashMap();
	// 是否中断收费操作
	private boolean discontinue = false;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Date instant = new Date();
		System.out.println(StrUtil.formatDate(instant, "yyyy-MM-dd HH:mm:ss"));
		DateTime d = new DateTime(instant);
		DateTime plusSeconds = d.plusSeconds(3);
		System.out.println(StrUtil.formatDate(plusSeconds.toDate(), "yyyy-MM-dd HH:mm:ss"));

	}

	public CarparkMainApp() {
		Object readObject = com.dongluhitec.card.ui.util.FileUtils.readObject("mapIpToDevice");
		if (readObject != null) {
			mapIpToDevice = (Map<String, SingleCarparkDevice>) readObject;
			for (String key : mapIpToDevice.keySet()) {
				SingleCarparkDevice singleCarparkDevice = mapIpToDevice.get(key);
				if (StrUtil.isEmpty(singleCarparkDevice.getInType())) {
					continue;
				}
				mapDeviceType.put(key, singleCarparkDevice.getInType());
			}
		}
		for (SystemSettingTypeEnum t : SystemSettingTypeEnum.values()) {
			mapSystemSetting.put(t, null);
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		userType = System.getProperty("userType");
		if (StrUtil.isEmpty(userType)) {
			System.exit(0);
		}
		init();
		Display display = Display.getDefault();
		createContents();
		shell.setMaximized(true);
		shell.setImage(JFaceUtil.getImage("carpark_16"));
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		commonui.confirm("提示", "车牌抓拍数：" + plateNoTotal.intValue(), new Shell());
		System.exit(0);
	}

	/**
	 * 初始化
	 */
	private void init() {
		presenter.setView(this);
		presenter.setMapDeviceTabItem(this.mapDeviceTabItem);
		presenter.setMapDeviceType(this.mapDeviceType);
		presenter.setMapIpToDevice(mapIpToDevice);
		presenter.setMapSystemSetting(mapSystemSetting);
		model = new CarparkMainModel();
		presenter.setModel(model);
		String userName = System.getProperty("userName");
		model.setUserName(userName);
		model.setWorkTime(new Date());
		List<SingleCarparkCarpark> findAllCarpark = sp.getCarparkService().findAllCarpark();
		int total = 0;
		int left = 0;
		for (SingleCarparkCarpark singleCarparkCarpark : findAllCarpark) {
			total += singleCarparkCarpark.getTotalNumberOfSlot();
			left += singleCarparkCarpark.getLeftNumberOfSlot();
		}

		CarparkInOutServiceI carparkInOutService = sp.getCarparkInOutService();
		model.setTotalSlot(carparkInOutService.findTotalSlotIsNow());
		model.setHoursSlot(carparkInOutService.findTempSlotIsNow());
		model.setMonthSlot(carparkInOutService.findFixSlotIsNow());
		model.setTotalCharge(carparkInOutService.findFactMoneyByName(userName));
		model.setTotalFree(carparkInOutService.findFreeMoneyByName(userName));
		List<SingleCarparkSystemSetting> findAllSystemSetting = sp.getCarparkService().findAllSystemSetting();
		for (SingleCarparkSystemSetting ss : findAllSystemSetting) {
			mapSystemSetting.put(SystemSettingTypeEnum.valueOf(ss.getSettingKey()), ss.getSettingValue());
		}
		com.dongluhitec.card.ui.util.FileUtils.writeObject(IMAGE_SAVE_SITE, mapSystemSetting.get(SystemSettingTypeEnum.图片保存位置));
		List<SingleCarparkBlackUser> findAllBlackUser = sp.getCarparkService().findAllBlackUser();
		for (SingleCarparkBlackUser singleCarparkBlackUser : findAllBlackUser) {
			mapBlackUser.put(singleCarparkBlackUser.getPlateNO(), singleCarparkBlackUser);
		}
//		autoSendPositionToDevice();
	}

	/**
	 * Create contents of the window.
	 * 
	 * @throws IOException
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setMinimumSize(new Point(1024, 768));
		shell.setSize(450, 300);
		shell.setText("停车场监控-1.0.0.1");
		GridLayout gl_shell = new GridLayout(2, false);
		gl_shell.verticalSpacing = 2;
		gl_shell.marginWidth = 2;
		gl_shell.marginHeight = 2;
		gl_shell.horizontalSpacing = 2;
		shell.setLayout(gl_shell);

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.VERTICAL));
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite.verticalIndent = 5;
		composite.setLayoutData(gd_composite);

		Composite composite_1 = new Composite(composite, SWT.NONE);
		FillLayout fl_composite_1 = new FillLayout(SWT.HORIZONTAL);
		fl_composite_1.spacing = 5;
		composite_1.setLayout(fl_composite_1);

		tabInFolder = new CTabFolder(composite_1, SWT.BORDER | SWT.FLAT);
		tabInFolder.setFont(SWTResourceManager.getFont("微软雅黑", 14, SWT.BOLD));
		Composite control = new Composite(tabInFolder, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		control.setLayout(layout);
		ToolBar toolBar = new ToolBar(control, SWT.NONE);
		ToolItem toolItem_in_photograph = new ToolItem(toolBar, SWT.NONE);
		toolItem_in_photograph.setText("拍照");
		toolItem_in_photograph.setToolTipText("进口手动抓拍");
		toolItem_in_photograph.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CTabItem selection = tabInFolder.getSelection();
				if (StrUtil.isEmpty(selection)) {
					return;
				}
				handPhotograph(mapDeviceTabItem.get(selection));
			}
		});
		ToolItem toolItem_in_openDoor = new ToolItem(toolBar, SWT.NONE);
		toolItem_in_openDoor.setText("抬杆");
		toolItem_in_openDoor.setToolTipText("进口手动抬杆");
		toolItem_in_openDoor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CTabItem selection = tabInFolder.getSelection();
				if (StrUtil.isEmpty(selection)) {
					return;
				}
				presenter.openDoor(mapIpToDevice.get(mapDeviceTabItem.get(selection)));
			}
		});
		if (!userType.equals("操作员")) {

			ToolItem addInToolItem = new ToolItem(toolBar, SWT.NONE);
			addInToolItem.setText("添加");
			addInToolItem.setToolTipText("添加进口设备");
			addInToolItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					presenter.addDevice(tabInFolder, "进口");
				}
			});
			ToolItem editInToolItem = new ToolItem(toolBar, SWT.NONE);
			editInToolItem.setText("修改");
			editInToolItem.setToolTipText("修改进口设备");
			editInToolItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					presenter.editDevice(tabInFolder, "进口");
				}
			});

			ToolItem delInToolItem = new ToolItem(toolBar, SWT.NONE);
			delInToolItem.setText("删除");
			delInToolItem.setToolTipText("删除进口设备");

			delInToolItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					presenter.deleteDeviceTabItem(tabInFolder.getSelection());
				}
			});
		}

		tabInFolder.setTopRight(control);
		tabInFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));

		tabOutFolder = new CTabFolder(composite_1, SWT.BORDER | SWT.FLAT);
		Composite control2 = new Composite(tabOutFolder, SWT.NONE);
		GridLayout layout2 = new GridLayout();
		layout2.marginHeight = 0;
		layout2.marginWidth = 0;
		control2.setLayout(layout2);
		ToolBar outToolBar = new ToolBar(control2, SWT.NONE);
		ToolItem toolItem_out_photograph = new ToolItem(outToolBar, SWT.NONE);
		toolItem_out_photograph.setText("拍照");
		toolItem_out_photograph.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CTabItem selection = tabOutFolder.getSelection();
				if (StrUtil.isEmpty(selection)) {
					return;
				}
				handPhotograph(mapDeviceTabItem.get(selection));
			}
		});
		ToolItem toolItem_out_openDoor = new ToolItem(outToolBar, SWT.NONE);
		toolItem_out_openDoor.setText("抬杆");
		toolItem_out_openDoor.setToolTipText("出口手动抬杆");
		toolItem_out_openDoor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CTabItem selection = tabOutFolder.getSelection();
				if (StrUtil.isEmpty(selection)) {
					return;
				}
				presenter.openDoor(mapIpToDevice.get(mapDeviceTabItem.get(selection)));
			}
		});
		if (!userType.equals("操作员")) {
			ToolItem addOutToolItem = new ToolItem(outToolBar, SWT.NONE);
			addOutToolItem.setText("添加");
			addOutToolItem.setToolTipText("添加出口设备");
			addOutToolItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					presenter.addDevice(tabOutFolder, "出口");
				}
			});
			ToolItem editOutToolItem = new ToolItem(outToolBar, SWT.NONE);
			editOutToolItem.setText("修改");
			editOutToolItem.setToolTipText("修改出口设备");
			editOutToolItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					presenter.editDevice(tabOutFolder, "出口");
				}
			});

			ToolItem delOutToolItem = new ToolItem(outToolBar, SWT.NONE);
			delOutToolItem.setText("删除");
			delOutToolItem.setToolTipText("删除出口设备");
			delOutToolItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					presenter.deleteDeviceTabItem(tabOutFolder.getSelection());
				}
			});
		}

		tabOutFolder.setTopRight(control2);
		tabOutFolder.setFont(SWTResourceManager.getFont("微软雅黑", 14, SWT.BOLD));
		tabOutFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));

		Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setLayout(new GridLayout(1, false));

		Composite composite_3 = new Composite(composite_2, SWT.NONE);
		composite_3.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite composite_5 = new Composite(composite_3, SWT.NONE);
		composite_5.setLayout(new GridLayout(2, false));

		Composite composite_9 = new Composite(composite_5, SWT.NONE);
		composite_9.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		composite_9.setLayout(new GridLayout(2, false));
		composite_9.setBounds(0, 0, 64, 64);

		Label lblNewLabel_3 = new Label(composite_9, SWT.NONE);
		lblNewLabel_3.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_3.setBounds(0, 0, 61, 17);
		lblNewLabel_3.setText("车牌号码");

		txtinplateNo = new Text(composite_9, SWT.BORDER);
		txtinplateNo.setEditable(false);
		txtinplateNo.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		txtinplateNo.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		txtinplateNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblNewLabel_4 = new Label(composite_9, SWT.NONE);
		lblNewLabel_4.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		lblNewLabel_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_4.setText("入场时间");

		text_in_time = new Text(composite_9, SWT.BORDER);
		text_in_time.setEditable(false);
		text_in_time.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		text_in_time.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		text_in_time.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite composite_10 = new Composite(composite_5, SWT.BORDER);
		GridData gd_composite_10 = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
		gd_composite_10.widthHint = 120;
		composite_10.setLayoutData(gd_composite_10);
		composite_10.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite_10.setBounds(0, 0, 64, 64);

		inSmallImg = new CLabel(composite_10, SWT.NONE);
		inSmallImg.setForeground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
		inSmallImg.setFont(SWTResourceManager.getFont("微软雅黑", 13, SWT.BOLD));
		inSmallImg.setAlignment(SWT.CENTER);
		inSmallImg.setText("入场车牌");

		Composite composite_6 = new Composite(composite_3, SWT.NONE);
		composite_6.setLayout(new GridLayout(2, false));

		Composite composite_12 = new Composite(composite_6, SWT.NONE);
		composite_12.setLayout(new GridLayout(2, false));
		composite_12.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Label label_15 = new Label(composite_12, SWT.NONE);
		label_15.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_15.setText("车牌号码");
		label_15.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));

		txtoutplateNo = new Text(composite_12, SWT.BORDER);
		txtoutplateNo.setEditable(false);
		txtoutplateNo.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		txtoutplateNo.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		txtoutplateNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label label_16 = new Label(composite_12, SWT.NONE);
		label_16.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_16.setText("出场时间");
		label_16.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));

		text_out_time = new Text(composite_12, SWT.BORDER);
		text_out_time.setEditable(false);
		text_out_time.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		text_out_time.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		text_out_time.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite composite_11 = new Composite(composite_6, SWT.BORDER);
		composite_11.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_composite_11 = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
		gd_composite_11.widthHint = 120;
		composite_11.setLayoutData(gd_composite_11);

		outSmallImg = new CLabel(composite_11, SWT.NONE);
		outSmallImg.setForeground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
		outSmallImg.setAlignment(SWT.CENTER);
		outSmallImg.setFont(SWTResourceManager.getFont("微软雅黑", 13, SWT.BOLD));
		outSmallImg.setText("出场车牌");

		Composite composite_4 = new Composite(composite_2, SWT.NONE);
		FillLayout fl_composite_4 = new FillLayout(SWT.HORIZONTAL);
		fl_composite_4.spacing = 6;
		composite_4.setLayout(fl_composite_4);
		composite_4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite composite_7 = new Composite(composite_4, SWT.BORDER);
		composite_7.setLayout(new FillLayout(SWT.HORIZONTAL));

		inBigImg = new CLabel(composite_7, SWT.NONE);
		inBigImg.setForeground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
		inBigImg.setFont(SWTResourceManager.getFont("微软雅黑", 23, SWT.BOLD));
		inBigImg.setAlignment(SWT.CENTER);
		inBigImg.setText("入场车牌");

		Composite composite_8 = new Composite(composite_4, SWT.BORDER);
		composite_8.setLayout(new FillLayout(SWT.HORIZONTAL));

		outBigImg = new CLabel(composite_8, SWT.NONE);
		outBigImg.setText("入场车牌");
		outBigImg.setForeground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
		outBigImg.setFont(SWTResourceManager.getFont("微软雅黑", 23, SWT.BOLD));
		outBigImg.setAlignment(SWT.CENTER);

		Group group = new Group(shell, SWT.SHADOW_IN);
		group.setFont(SWTResourceManager.getFont("微软雅黑", 5, SWT.NORMAL));
		GridLayout gl_group = new GridLayout(2, false);
		gl_group.verticalSpacing = 8;
		group.setLayout(gl_group);
		GridData gd_group = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
		gd_group.widthHint = 250;
		group.setLayoutData(gd_group);

		lblNewLabel = new Label(group, SWT.NONE);
		lblNewLabel.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.BOLD));
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("剩余车位数");

		text_total = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		text_total.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		text_total.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		text_total.setEditable(false);
		text_total.setText("1000");
		text_total.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		text_total.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label label = new Label(group, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("时租车位数");
		label.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.BOLD));

		text_hours = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		text_hours.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		text_hours.setText("1000");
		text_hours.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		text_hours.setEditable(false);
		text_hours.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label label_1 = new Label(group, SWT.NONE);
		label_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_1.setText("月租车位数");
		label_1.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.BOLD));

		text_month = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		text_month.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		text_month.setText("1000");
		text_month.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		text_month.setEditable(false);
		text_month.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label label_2 = new Label(group, SWT.NONE);
		label_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_2.setText("当前值班");
		label_2.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.BOLD));

		txt_userName = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		txt_userName.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		txt_userName.setText("panmingzhi");
		txt_userName.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		txt_userName.setEditable(false);
		txt_userName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label label_3 = new Label(group, SWT.NONE);
		label_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_3.setText("上班时间");
		label_3.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.BOLD));

		text_worTime = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		text_worTime.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		text_worTime.setText("2015-8-15 12:30:20");
		text_worTime.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		text_worTime.setEditable(false);
		text_worTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label label_4 = new Label(group, SWT.NONE);
		label_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_4.setText("收费金额");
		label_4.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.BOLD));

		text_charge = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		text_charge.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		text_charge.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		text_charge.setText("1000");
		text_charge.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		text_charge.setEditable(false);
		text_charge.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label label_5 = new Label(group, SWT.NONE);
		label_5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_5.setText("免费金额");
		label_5.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.BOLD));

		text_free = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		text_free.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		text_free.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		text_free.setText("1000");
		text_free.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		text_free.setEditable(false);
		text_free.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblNewLabel_1 = new Label(group, SWT.SEPARATOR | SWT.HORIZONTAL);
		lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		lblNewLabel_1.setText("New Label");

		Label label_6 = new Label(group, SWT.NONE);
		label_6.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_6.setText("车牌号码");
		label_6.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.BOLD));

		txt_plateNO = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		txt_plateNO.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		txt_plateNO.setText("京A23456");
		txt_plateNO.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		txt_plateNO.setEditable(false);
		txt_plateNO.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label label_7 = new Label(group, SWT.NONE);
		label_7.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_7.setText("用户名称");
		label_7.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.BOLD));

		text_carName = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		text_carName.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		text_carName.setText("李大钊");
		text_carName.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		text_carName.setEditable(false);
		text_carName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label label_8 = new Label(group, SWT.NONE);
		label_8.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_8.setText("用户类型");
		label_8.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.BOLD));

		text_carType = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		text_carType.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		text_carType.setText("月卡");
		text_carType.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		text_carType.setEditable(false);
		text_carType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label label_9 = new Label(group, SWT.NONE);
		label_9.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_9.setText("入场时间");
		label_9.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.BOLD));

		text_intime = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		text_intime.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		text_intime.setText("2015-8-15 12:30:20");
		text_intime.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		text_intime.setEditable(false);
		text_intime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label label_10 = new Label(group, SWT.NONE);
		label_10.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_10.setText("出场时间");
		label_10.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.BOLD));

		text_outTime = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		text_outTime.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		text_outTime.setText("2015-8-15 14:50:20");
		text_outTime.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		text_outTime.setEditable(false);
		text_outTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label label_11 = new Label(group, SWT.NONE);
		label_11.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_11.setText("停车时间");
		label_11.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.BOLD));

		text_totalTime = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		text_totalTime.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		text_totalTime.setText("2:20:00");
		text_totalTime.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		text_totalTime.setEditable(false);
		text_totalTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label label_12 = new Label(group, SWT.NONE);
		label_12.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_12.setText("应收金额");
		label_12.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.BOLD));

		text_should = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		text_should.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		text_should.setText("20.0");
		text_should.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		text_should.setEditable(false);
		text_should.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label label_13 = new Label(group, SWT.NONE);
		label_13.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_13.setText("实收金额");
		label_13.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.BOLD));

		text_real = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		text_real.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				// 收费放行
				if (e.keyCode == 16777296 || e.keyCode == 13 || e.keyCode == 16777236) {
					chargeCarPass();
				}
				// 免费放行
				if (e.keyCode == 16777237) {
					freeCarPass();
				}
			}
		});
		text_real.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		text_real.setText("20.0");
		text_real.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		text_real.setEditable(true);
		text_real.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		btnNewButton = new Button(group, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				chargeCarPass();
			}
		});
		btnNewButton.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		GridData gd_btnNewButton = new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1);
		gd_btnNewButton.widthHint = 120;
		btnNewButton.setLayoutData(gd_btnNewButton);
		btnNewButton.setText("收费放行(F11)");

		btnf = new Button(group, SWT.NONE);
		btnf.addSelectionListener(new SelectionAdapter() {
			private boolean continueCharge;

			@Override
			public void widgetSelected(SelectionEvent e) {
				freeCarPass();
			}
		});
		btnf.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		GridData gd_btnf = new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1);
		gd_btnf.widthHint = 120;
		btnf.setLayoutData(gd_btnf);
		btnf.setText("免费放行(F12)");

		Button btnf_1 = new Button(group, SWT.NONE);
		btnf_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				presenter.changeUser();
			}
		});
		btnf_1.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		GridData gd_btnf_1 = new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1);
		gd_btnf_1.widthHint = 120;
		btnf_1.setLayoutData(gd_btnf_1);
		btnf_1.setText("换班(F7)");

		Button btnf_2 = new Button(group, SWT.NONE);
		btnf_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				presenter.returnAccount();
			}
		});
		btnf_2.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		GridData gd_btnf_2 = new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1);
		gd_btnf_2.widthHint = 120;
		btnf_2.setLayoutData(gd_btnf_2);
		btnf_2.setText("归账(F8)");

		Button btnf_3 = new Button(group, SWT.NONE);
		btnf_3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				presenter.showSearchInOutHistory();
			}
		});
		btnf_3.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.BOLD));
		GridData gd_btnf_3 = new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1);
		gd_btnf_3.widthHint = 120;
		btnf_3.setLayoutData(gd_btnf_3);
		btnf_3.setText("浏览记录(F9)");
		new Label(group, SWT.NONE);
		new Label(group, SWT.NONE);
		createDeviceTabItem();

		// mapDeviceType.put("192.168.1.138", "进口");
		shell.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println(e.keyCode);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println(e.keyCode);
			}
		});
		tabInFolder.setSelection(0);
		tabOutFolder.setSelection(0);
		m_bindingContext = initDataBindings();
	}

	protected void handPhotograph(String ip) {
		presenter.handPhotograph(ip);
		mapHandPhotograph.put(ip, new Date());
	}
	/**
	 * 没隔5秒自动发送车位
	 */
	private void autoSendPositionToDevice() {
		ScheduledExecutorService newSingleThreadScheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		newSingleThreadScheduledExecutor.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				Set<String> keySet = mapIpToDevice.keySet();
				for (String c : keySet) {
					presenter.showPositionToDevice(mapIpToDevice.get(c), model.getHoursSlot());
				}
			}
		}, 5000, 5000, TimeUnit.MILLISECONDS);
	}

	// private void addPool(byte[] readAllBytes) {
	//
	// Future<?> submit = execute.submit(new Runnable() {
	// @Override
	// public void run() {
	// invok("192.168.1.138", 1, "no", readAllBytes, readAllBytes);
	// }
	// });
	// try {
	// submit.get();
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// } catch (ExecutionException e) {
	// e.printStackTrace();
	// }
	// }

	// 创建设备的监控tab页
	private void createDeviceTabItem() {
		Set<String> keySet = mapDeviceType.keySet();
		for (String ip : keySet) {
			String type = mapDeviceType.get(ip);
			SingleCarparkDevice singleCarparkDevice = mapIpToDevice.get(ip);
			if (type.equals("进口")) {
				final CTabItem tabItem = new CTabItem(tabInFolder, SWT.NONE);
				tabItem.setFont(SWTResourceManager.getFont("微软雅黑", 15, SWT.NORMAL));
				tabItem.setText(singleCarparkDevice.getName() == null ? ip : singleCarparkDevice.getName());
				final Composite composite = new Composite(tabInFolder, SWT.BORDER | SWT.EMBEDDED);
				tabItem.setControl(composite);
				composite.setLayout(new FillLayout());
				presenter.createLeftCamera(ip, composite);
				mapDeviceTabItem.put(tabItem, ip);
				tabItem.addDisposeListener(new DisposeListener() {

					public void widgetDisposed(DisposeEvent e) {
						composite.dispose();
					}
				});
				
			} else if (type.equals("出口")) {
				CTabItem tabItem = new CTabItem(tabOutFolder, SWT.NONE);
				tabItem.setFont(SWTResourceManager.getFont("微软雅黑", 15, SWT.NORMAL));
				tabItem.setText(singleCarparkDevice.getName() == null ? ip : singleCarparkDevice.getName());
				final Composite composite = new Composite(tabOutFolder, SWT.BORDER | SWT.EMBEDDED);
				tabItem.setControl(composite);
				composite.setLayout(new FillLayout());
				presenter.createRightCamera(ip, composite);
				mapDeviceTabItem.put(tabItem, ip);
				tabItem.addDisposeListener(new DisposeListener() {

					public void widgetDisposed(DisposeEvent e) {
						composite.dispose();
					}
				});
			}
			presenter.showUsualContentToDevice(singleCarparkDevice, USUAL_MSG);
		}

	}

	/**
	 * 获取图片
	 * 
	 * @param smallImage
	 * @param insmallimg
	 * @param shell
	 * @return
	 */
	public Image getImage(final byte[] smallImage, CLabel insmallimg, Shell shell) {
		if (smallImage == null) {
			insmallimg.setText("无图片");
			return null;
		}

		ByteArrayInputStream stream = null;
		try {
			stream = new ByteArrayInputStream(smallImage);
			Image img = new Image(shell.getDisplay(), stream);
			Rectangle rectangle = insmallimg.getBounds();
			ImageData data = img.getImageData().scaledTo(rectangle.width, rectangle.height);
			ImageDescriptor createFromImageData = ImageDescriptor.createFromImageData(data);
			Image createImg = createFromImageData.createImage();
			img.dispose();
			img = null;
			insmallimg.setText("");
			return createImg;
		} catch (Exception e) {
			throw new DongluAppException("图片转换错误", e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 车牌识别监控
	 */
	public void invok(final String ip, int channel, final String plateNO, final byte[] bigImage, final byte[] smallImage) {
		Preconditions.checkNotNull(mapDeviceType.get(ip), "not monitor device:" + ip);
		if (mapDeviceType.get(ip).equals("出口")) {
			new Thread(new Runnable() {
				public void run() {
					carparkOutTask(ip, plateNO, bigImage, smallImage);
				}
			}).start();
		} else if (mapDeviceType.get(ip).equals("进口")) {
			new Thread(new Runnable() {
				public void run() {
					carparkInTask(ip, plateNO, bigImage, smallImage);
				}
			}).start();

		}
	}

	// 停车场进
	private synchronized void carparkInTask(final String ip, final String plateNO, final byte[] bigImage, final byte[] smallImage) {
		LOGGER.debug("开始保存车牌：{}的图片", plateNO);
		long nanoTime = System.nanoTime();
		Date date = new Date();
		String folder = StrUtil.formatDate(date, "yyyy/MM/dd/HH");
		String fileName = StrUtil.formatDate(date, "yyyyMMddHHmmssSSS");
		String bigImgFileName = fileName + "_" + plateNO + "_big.jpg";
		String smallImgFileName = fileName + "_" + plateNO + "_small.jpg";
		presenter.saveImage(folder, bigImgFileName, bigImage);
		presenter.saveImage(folder, smallImgFileName, smallImage);
		long nanoTime1 = System.nanoTime();

		final String dateString = StrUtil.formatDate(date, "yyyy-MM-dd HH:mm:ss");
		LOGGER.info(dateString + "==" + ip + "==" + mapDeviceType.get(ip) + "==" + plateNO);
		SingleCarparkDevice device = mapIpToDevice.get(ip);
		if (StrUtil.isEmpty(device)) {
			LOGGER.error("没有找到ip:" + ip + "的设备");
			return;
		}
		LOGGER.debug("开始在界面显示车牌：{}的抓拍图片", plateNO);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (inSmallImage != null) {
					LOGGER.info(dateString + ip + "小图片销毁图片");
					inSmallImage.dispose();
					inSmallImg.setBackgroundImage(null);
				}
				if (inBigImage != null) {
					LOGGER.info(dateString + ip + "大图片销毁图片");
					inBigImage.dispose();
					inBigImg.setBackgroundImage(null);
				}

				inSmallImage = getImage(smallImage, inSmallImg, shell);
				if (inSmallImage != null) {
					inSmallImg.setBackgroundImage(inSmallImage);
				}

				inBigImage = getImage(bigImage, inBigImg, shell);
				if (inBigImage != null) {
					inBigImg.setBackgroundImage(inBigImage);
				}

				txtinplateNo.setText(plateNO);
				text_in_time.setText(dateString);
				plateNoTotal.addAndGet(1);
			}
		});
		//空车牌处理
		if (StrUtil.isEmpty(plateNO)) {
			LOGGER.info("空的车牌");
			return;
		}
		// model.setInShowBigImg(inBigImage);
		// model.setInShowSmallImg(inSmallImage);
		// model.setInShowPlateNO(plateNO);
		// model.setInShowTime(dateString);
		// plateNoTotal.addAndGet(1);
		 
		long nanoTime3 = System.nanoTime();
		LOGGER.debug("进行黑名单判断");
		SingleCarparkBlackUser singleCarparkBlackUser = mapBlackUser.get(plateNO);
		if (!StrUtil.isEmpty(singleCarparkBlackUser)) {
			LOGGER.error("车牌：{}为黑名单", plateNO);
			model.setInShowMeg("黑名单");
			return;
		}
		LOGGER.debug("显示车牌");
		presenter.showPlateNOToDevice(device, plateNO);
		
		LOGGER.debug("查找是否为固定车");
		List<SingleCarparkUser> findByNameOrPlateNo = sp.getCarparkUserService().findByNameOrPlateNo(null, plateNO);
		SingleCarparkUser user = StrUtil.isEmpty(findByNameOrPlateNo) ? null : findByNameOrPlateNo.get(0);
		
		String carType = "临时车";
		if (!StrUtil.isEmpty(user)) {
			carType = "固定车";
//			if (model.getMonthSlot() <= 0) {
//				LOGGER.error("固定车位已满");
//				return;
//			}
			String content = StrUtil.formatDate(user.getValidTo(), VILIDTO_DATE);
			presenter.showContentToDevice(device, CAR_IN_MSG+content, 1);
			LOGGER.info("固定车：{}，{}",plateNO,content);
		} else {
			LOGGER.debug("判断是否允许临时车进");
			if (device.getCarpark().isTempCarIsIn()) {
				presenter.showContentToDevice(device, "固定停车场,不允许临时车进", 1);
				presenter.showUsualContentToDevice(device, USUAL_MSG);
				return;
			}
			presenter.showContentToDevice(device, CAR_IN_MSG, 1);
		}
		if (model.getHoursSlot() <= 0) {
			LOGGER.error("临时车位已满");
			return;
		}
		LOGGER.debug("车辆类型为：{}==t通道类型为：{}", carType, device.getRoadType());
		// showInDevice(device, plateNO, user);
		long nanoTime2 = System.nanoTime();
		LOGGER.debug(dateString + "==" + ip + "==" + mapDeviceType.get(ip) + "==" + plateNO + "车辆类型：" + carType + "==" + "保存图片：" + (nanoTime1 - nanoTime) + "==查找固定用户：" + (nanoTime2 - nanoTime3)
				+ "==界面操作：" + (nanoTime3 - nanoTime1));
		LOGGER.info("把车牌:{}的进场记录保存到数据库",plateNO);
		SingleCarparkInOutHistory h = new SingleCarparkInOutHistory();
		h.setInTime(date);
		h.setPlateNo(plateNO);
		h.setOperaName(System.getProperty("userName"));
		h.setBigImg(folder + "/" + bigImgFileName);
		h.setSmallImg(folder + "/" + smallImgFileName);
		h.setCarType(carType);
		if (!StrUtil.isEmpty(user)) {
			h.setUserName(user.getName());
			h.setUserId(user.getId());
		}
		h.setInDevice(device.getName());
		h.setInPhotographType("自动");
		Date handPhotographDate = mapHandPhotograph.get(ip);
		if (!StrUtil.isEmpty(handPhotographDate)) {
			DateTime plusSeconds = new DateTime(handPhotographDate).plusSeconds(3);
			boolean after = plusSeconds.toDate().after(date);
			if (after)
				h.setInPhotographType("手动");
		}
		CarparkService carparkService = sp.getCarparkService();
		SingleCarparkCarpark carpark = carparkService.findCarparkById(device.getCarpark().getId());

		if (carType.equals("临时车")) {
			model.setHoursSlot(model.getHoursSlot() - 1);
		} else {
			if (model.getMonthSlot()<=0) {
				model.setHoursSlot(model.getHoursSlot() - 1);
			}else
			model.setMonthSlot(model.getMonthSlot() - 1);
		}
		int left = carpark.getLeftNumberOfSlot() - 1;
		carpark.setLeftNumberOfSlot(left <= 0 ? 0 : left);
		int total = model.getTotalSlot() - 1;
		model.setTotalSlot(total <= 0 ? 0 : total);
		carparkService.saveCarpark(carpark);
		sp.getCarparkInOutService().saveInOutHistory(h);
		presenter.openDoor(device);
//		presenter.showUsualContentToDevice(device, USUAL_MSG);
	}

	// 停车场出
	private synchronized void carparkOutTask(final String ip, final String plateNO, final byte[] bigImage, final byte[] smallImage) {

		long nanoTime = System.nanoTime();
		Date date = new Date();
		String folder = StrUtil.formatDate(date, "yyyy/MM/dd/HH");
		String fileName = StrUtil.formatDate(date, "yyyyMMddHHmmssSSS");
		String bigImgFileName = fileName + "_" + plateNO + "_big.jpg";
		presenter.saveImage(folder, bigImgFileName, bigImage);
		String smallImgFileName = fileName + "_" + plateNO + "_small.jpg";
		presenter.saveImage(folder, smallImgFileName, smallImage);
		long nanoTime1 = System.nanoTime();
		final String dateString = StrUtil.formatDate(date, "yyyy-MM-dd HH:mm:ss");
		// System.out.println(dateString + "==" + ip + "==" + mapDeviceType.get(ip) + "==" + plateNO);
		LOGGER.info(dateString + "==" + ip + "==" + mapDeviceType.get(ip) + "==" + plateNO);
		// 界面图片
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (outSmallImage != null) {
					LOGGER.info(dateString + ip + "小图片销毁图片");
					outSmallImage.dispose();
					outSmallImg.setBackgroundImage(null);
				}
				if (outBigImage != null) {
					LOGGER.info(dateString + ip + "大图片销毁图片");
					outBigImage.dispose();
					outBigImg.setBackgroundImage(null);
				}

				outSmallImage = getImage(smallImage, outSmallImg, shell);
				if (outSmallImage != null) {
					outSmallImg.setBackgroundImage(outSmallImage);
				}

				outBigImage = getImage(bigImage, outBigImg, shell);
				if (outBigImage != null) {
					outBigImg.setBackgroundImage(outBigImage);
				}

				txtoutplateNo.setText(plateNO);
				text_out_time.setText(dateString);
				text_real.setFocus();
				text_real.selectAll();
				plateNoTotal.addAndGet(1);
			}
		});

		SingleCarparkDevice device = mapIpToDevice.get(ip);
		if (StrUtil.isEmpty(device)) {
			LOGGER.info("没有找到ip为：" + ip + "的设备");
			return;
		}
		presenter.showPlateNOToDevice(device, plateNO);

		long nanoTime3 = System.nanoTime();
		List<SingleCarparkUser> findByNameOrPlateNo = sp.getCarparkUserService().findByNameOrPlateNo(null, plateNO);
		SingleCarparkUser user = StrUtil.isEmpty(findByNameOrPlateNo) ? null : findByNameOrPlateNo.get(0);
		String carType = "临时车";
		if (!StrUtil.isEmpty(user)) {
			carType = "固定车";
		}
		String roadType = device.getRoadType();
		LOGGER.info("车辆类型为：{}==通道类型为：{}", carType, roadType);
		// System.out.println("=====车辆类型为："+carType+"通道类型为："+roadType);
		long nanoTime2 = System.nanoTime();
		LOGGER.info(dateString + "==" + ip + "==" + mapDeviceType.get(ip) + "==" + plateNO + "车辆类型：" + carType + "\n" + "保存图片：" + (nanoTime1 - nanoTime) + "==查找固定用户：" + (nanoTime2 - nanoTime3)
				+ "==界面操作：" + (nanoTime3 - nanoTime1));
		boolean equals = roadType.equals(DeviceRoadTypeEnum.固定车通道.name());

		if (carType.equals("固定车")) {
			if (!equals) {
				if (roadType.equals(DeviceRoadTypeEnum.临时车通道.name())) {
					presenter.showContentToDevice(device, TEMP_ROAD, 1);
					return;
				}
			}
			Date validTo = user.getValidTo();
			Integer delayDays = user.getDelayDays() == null ? 0 : user.getDelayDays();
			Calendar c = Calendar.getInstance();
			c.setTime(validTo);
			c.add(Calendar.DATE, delayDays);
			Date time = c.getTime();

			if (StrUtil.getTodayBottomTime(time).before(date)) {
				presenter.showContentToDevice(device, CAR_IS_ARREARS + StrUtil.formatDate(user.getValidTo(),VILIDTO_DATE), 1);
				LOGGER.info("车辆:{}已到期", plateNO);
				return;
			} else {
				c.setTime(validTo);
				c.add(Calendar.DATE, user.getRemindDays() == null ? 0 : user.getRemindDays());
				time = c.getTime();
				if (StrUtil.getTodayBottomTime(time).before(date)) {
					presenter.showContentToDevice(device, CAR_WILL_ARREARS + StrUtil.formatDate(user.getValidTo(),VILIDTO_DATE), 1);
					LOGGER.info("车辆:{}即将到期", plateNO);
				} else {
					presenter.showContentToDevice(device, CAR_OUT_MSG + StrUtil.formatDate(user.getValidTo()), 1);
				}
			}

			SingleCarparkCarpark carpark = sp.getCarparkService().findCarparkById(device.getCarpark().getId());
			carpark.setLeftNumberOfSlot(carpark.getLeftNumberOfSlot() + 1);
			model.setMonthSlot(model.getMonthSlot() + 1 > carpark.getFixNumberOfSlot() ? carpark.getFixNumberOfSlot() : model.getMonthSlot() + 1);
			model.setTotalSlot(model.getHoursSlot() + model.getMonthSlot());
			presenter.openDoor(device);
		} else {// 临时车操作
			// 固定车通道
			if (equals) {
				// presenter.showPlateNOToDevice(device, FIX_ROAD, 1, 1);
				return;
			}
			carparkOutProcess(ip, plateNO, device, date);
		}
	}

	private void carparkOutProcess(final String ip, final String plateNO, SingleCarparkDevice device, Date date) {
		// System.out.println("出场");
		CarparkInOutServiceI carparkInOutService = sp.getCarparkInOutService();
		List<SingleCarparkInOutHistory> findByNoCharge = carparkInOutService.findByNoOut(plateNO);
		// System.out.println("入场纪录："+findByNoCharge.size());
		if (StrUtil.isEmpty(findByNoCharge)) {
			// 手动识别
			// System.out.println("没有找到车牌" + plateNO + "的进场记录");
			// String input = commonui.input("请输入车牌", "");
			// InputDialog d=new InputDialog(new Shell(), "输入车牌", "", "", null);
			// int open = d.open();
			// carparkInOutService.findByNoOut(input);
			LOGGER.error("没有找到车牌：{}的进场记录", plateNO);
			return;

		}

		if (!StrUtil.isEmpty(findByNoCharge)) {

			SingleCarparkInOutHistory singleCarparkInOutHistory = findByNoCharge.get(0);
			String type = singleCarparkInOutHistory.getCarType();

			singleCarparkInOutHistory.setOutTime(date);
			singleCarparkInOutHistory.setOperaName(model.getUserName());
			singleCarparkInOutHistory.setOutDevice(device.getName());
			singleCarparkInOutHistory.setOutPhotographType("自动");
			Date handPhotographDate = mapHandPhotograph.get(ip);
			if (!StrUtil.isEmpty(handPhotographDate)) {
				DateTime plusSeconds = new DateTime(handPhotographDate).plusSeconds(3);
				boolean after = plusSeconds.toDate().after(date);
				if (after)
					singleCarparkInOutHistory.setOutPhotographType("手动");
			}
			Date inTime = singleCarparkInOutHistory.getInTime();

			if (type.equals("临时车")) {
				// 临时车操作
				model.setPlateNo(plateNO);
				model.setCarType(singleCarparkInOutHistory.getCarType());
				model.setBtnClick(true);
				model.setOutTime(date);
				model.setInTime(inTime);
				model.setTotalTime(StrUtil.MinusTime2(inTime, date));
				model.setHistory(singleCarparkInOutHistory);
				float shouldMoney = presenter.countShouldMoney();
				model.setShouldMony(shouldMoney);
				LOGGER.info("{}进场时间{}，出场时间{}，停车：{}，应收费：{}元", plateNO, model.getInTime(), model.getOutTime(), model.getTotalTime(), shouldMoney);
				presenter.showContentToDevice(device, "请缴费" + shouldMoney + "元", 1);
				String property = System.getProperty(TEMP_CAR_AUTO_PASS);
				Boolean valueOf = Boolean.valueOf(property);
				// 自动放行
				if (!valueOf) {
					while (model.isBtnClick()) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if (discontinue) {
						return;
					}
				} else {
					// 测试添加默认实收
					model.setReal(15);
				}

				float factMoney = model.getReal();

				singleCarparkInOutHistory.setShouldMoney(shouldMoney);

				singleCarparkInOutHistory.setFactMoney(factMoney);
				float freeMoney = shouldMoney - factMoney;
				singleCarparkInOutHistory.setFreeMoney(freeMoney);
				String userName = System.getProperty("userName");
				// System.out.println("singleCarparkInOutHistory.getFreeMoney()=="+singleCarparkInOutHistory.getFreeMoney());
				Long saveInOutHistory = carparkInOutService.saveInOutHistory(singleCarparkInOutHistory);
				model.setHistory(singleCarparkInOutHistory);
				// model.setTotalCharge(sp.getCarparkInOutService().findFactMoneyByName(userName));
				// model.setTotalFree(sp.getCarparkInOutService().findFreeMoneyByName(userName));
				model.setTotalCharge(model.getTotalCharge() + factMoney);
				model.setTotalFree(model.getTotalFree() + freeMoney);
				presenter.openDoor(device);
				model.setTotalSlot(model.getTotalSlot() + 1);
				model.setHoursSlot(model.getHoursSlot() + 1);
				model.setPlateNo("");
				model.setShouldMony(0);
				model.setReal(0);
				model.setInTime(null);
				model.setOutTime(null);
				model.setCarType(null);
				model.setTotalTime(null);
			} else {
				// 固定车操作
				carparkInOutService.saveInOutHistory(singleCarparkInOutHistory);
			}

		}
	}

	@Override
	public boolean isOpen() {
		// TODO 自动生成的方法存根
		return false;
	}

	protected DataBindingContext initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		//
		IObservableValue observeTextText_totalObserveWidget = WidgetProperties.text(SWT.Modify).observe(text_total);
		IObservableValue totalSlotModelObserveValue = BeanProperties.value("totalSlot").observe(model);
		bindingContext.bindValue(observeTextText_totalObserveWidget, totalSlotModelObserveValue, null, null);
		//
		IObservableValue observeTextText_hoursObserveWidget = WidgetProperties.text(SWT.Modify).observe(text_hours);
		IObservableValue hoursSlotModelObserveValue = BeanProperties.value("hoursSlot").observe(model);
		bindingContext.bindValue(observeTextText_hoursObserveWidget, hoursSlotModelObserveValue, null, null);
		//
		IObservableValue observeTextText_monthObserveWidget = WidgetProperties.text(SWT.Modify).observe(text_month);
		IObservableValue monthSlotModelObserveValue = BeanProperties.value("monthSlot").observe(model);
		bindingContext.bindValue(observeTextText_monthObserveWidget, monthSlotModelObserveValue, null, null);
		//
		IObservableValue observeTextTxt_userNameObserveWidget = WidgetProperties.text(SWT.Modify).observe(txt_userName);
		IObservableValue userNameModelObserveValue = BeanProperties.value("userName").observe(model);
		bindingContext.bindValue(observeTextTxt_userNameObserveWidget, userNameModelObserveValue, null, null);
		//
		IObservableValue observeTextText_worTimeObserveWidget = WidgetProperties.text(SWT.Modify).observe(text_worTime);
		IObservableValue workTimeModelObserveValue = BeanProperties.value("workTime").observe(model);
		bindingContext.bindValue(observeTextText_worTimeObserveWidget, workTimeModelObserveValue, null, null);
		//
		IObservableValue observeTextText_chargeObserveWidget = WidgetProperties.text(SWT.Modify).observe(text_charge);
		IObservableValue totalChargeModelObserveValue = BeanProperties.value("totalCharge").observe(model);
		bindingContext.bindValue(observeTextText_chargeObserveWidget, totalChargeModelObserveValue, null, null);
		//
		IObservableValue observeTextText_freeObserveWidget = WidgetProperties.text(SWT.Modify).observe(text_free);
		IObservableValue totalFreeModelObserveValue = BeanProperties.value("totalFree").observe(model);
		bindingContext.bindValue(observeTextText_freeObserveWidget, totalFreeModelObserveValue, null, null);
		//
		IObservableValue observeTextTxt_plateNOObserveWidget = WidgetProperties.text(SWT.Modify).observe(txt_plateNO);
		IObservableValue plateNoModelObserveValue = BeanProperties.value("plateNo").observe(model);
		bindingContext.bindValue(observeTextTxt_plateNOObserveWidget, plateNoModelObserveValue, null, null);
		//
		IObservableValue observeTextText_carNameObserveWidget = WidgetProperties.text(SWT.Modify).observe(text_carName);
		IObservableValue carUserModelObserveValue = BeanProperties.value("carUser").observe(model);
		bindingContext.bindValue(observeTextText_carNameObserveWidget, carUserModelObserveValue, null, null);
		//
		IObservableValue observeTextText_carTypeObserveWidget = WidgetProperties.text(SWT.Modify).observe(text_carType);
		IObservableValue carTypeModelObserveValue = BeanProperties.value("carType").observe(model);
		bindingContext.bindValue(observeTextText_carTypeObserveWidget, carTypeModelObserveValue, null, null);
		//
		IObservableValue observeTextText_intimeObserveWidget = WidgetProperties.text(SWT.Modify).observe(text_intime);
		IObservableValue inTimeModelObserveValue = BeanProperties.value("inTime").observe(model);
		bindingContext.bindValue(observeTextText_intimeObserveWidget, inTimeModelObserveValue, null, null);
		//
		IObservableValue observeTextText_outTimeObserveWidget = WidgetProperties.text(SWT.Modify).observe(text_outTime);
		IObservableValue outTimeModelObserveValue = BeanProperties.value("outTime").observe(model);
		bindingContext.bindValue(observeTextText_outTimeObserveWidget, outTimeModelObserveValue, null, null);
		//
		IObservableValue observeTextText_totalTimeObserveWidget = WidgetProperties.text(SWT.Modify).observe(text_totalTime);
		IObservableValue totalTimeModelObserveValue = BeanProperties.value("totalTime").observe(model);
		bindingContext.bindValue(observeTextText_totalTimeObserveWidget, totalTimeModelObserveValue, null, null);
		//
		IObservableValue observeTextText_shouldObserveWidget = WidgetProperties.text(SWT.Modify).observe(text_should);
		IObservableValue shouldMonyModelObserveValue = BeanProperties.value("shouldMony").observe(model);
		bindingContext.bindValue(observeTextText_shouldObserveWidget, shouldMonyModelObserveValue, null, null);
		//
		IObservableValue observeTextText_realObserveWidget = WidgetProperties.text(SWT.Modify).observe(text_real);
		IObservableValue realModelObserveValue = BeanProperties.value("real").observe(model);
		bindingContext.bindValue(observeTextText_realObserveWidget, realModelObserveValue, null, null);
		//
		IObservableValue observeEnabledBtnNewButtonObserveWidget = WidgetProperties.enabled().observe(btnNewButton);
		IObservableValue btnClickModelObserveValue = BeanProperties.value("btnClick").observe(model);
		bindingContext.bindValue(observeEnabledBtnNewButtonObserveWidget, btnClickModelObserveValue, null, null);
		//
		IObservableValue observeEnabledBtnfObserveWidget = WidgetProperties.enabled().observe(btnf);
		bindingContext.bindValue(observeEnabledBtnfObserveWidget, btnClickModelObserveValue, null, null);
		//
		IObservableValue observeTextInBigImgObserveWidget = WidgetProperties.text().observe(inBigImg);
		IObservableValue inShowMegModelObserveValue = BeanProperties.value("inShowMeg").observe(model);
		bindingContext.bindValue(observeTextInBigImgObserveWidget, inShowMegModelObserveValue, null, null);
		//
		return bindingContext;
	}

	// 收费
	private void chargeCarPass() {
		if (!model.isBtnClick()) {
			System.out.println("不能收费");
			return;
		}
		boolean chargeCarPass = presenter.chargeCarPass();
		if (!chargeCarPass) {
			return;
		}
		model.setBtnClick(false);
	}

	// 免费
	private void freeCarPass() {
		if (!model.isBtnClick()) {
			System.out.println("不能收费");
			return;
		}
		boolean freeCarPass = presenter.freeCarPass();
		if (!freeCarPass) {
			return;
		}
		model.setBtnClick(false);
	}
}
