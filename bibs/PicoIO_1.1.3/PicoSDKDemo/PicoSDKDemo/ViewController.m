
#import "ViewController.h"


@interface ViewController ()
{
    UIView *_layPico;
    UILabel *_lblPico;

    UILabel *_lblScan;
    NSMutableArray *_lblMatches;

    UIScrollView *_scroll;
    UIView *_scrollContent;
    UILabel *_lblLog;

    UIButton *_btnScan;
    UIButton *_btnCalibrate;
    UIButton *_btnConnect;
    UIButton *_btnDisconnect;

    CUPicoConnector *_picoConnector;
    CUPico *_pico;
}
@end


@implementation ViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self initViews];

    _picoConnector = CUPicoConnector.alloc.init;
    _picoConnector.delegate = self;
}

- (void)initViews
{
    // Setup views
    self.view.backgroundColor = UIColor.whiteColor;

    _layPico = UIView.alloc.init;
    _layPico.translatesAutoresizingMaskIntoConstraints = NO;
    _layPico.backgroundColor = [UIColor colorWithWhite:0.9 alpha:1];
    [self.view addSubview:_layPico];

    _lblPico = UILabel.alloc.init;
    _lblPico.translatesAutoresizingMaskIntoConstraints = NO;
    _lblPico.numberOfLines = 0;
    _lblPico.text = @"Pico disconnected";
    [_layPico addSubview:_lblPico];

    _lblScan = UILabel.alloc.init;
    _lblScan.translatesAutoresizingMaskIntoConstraints = NO;
    _lblScan.backgroundColor = _layPico.backgroundColor;
    _lblScan.textAlignment = NSTextAlignmentCenter;
    _lblScan.font = [UIFont fontWithName:_lblScan.font.fontName size:12];
    _lblScan.text = @"SCAN";
    [self.view addSubview:_lblScan];

    _lblMatches = NSMutableArray.array;
    for (NSInteger i = 0; i < 3; i++)
    {
        UILabel *lbl = UILabel.alloc.init;
        lbl.translatesAutoresizingMaskIntoConstraints = NO;
        lbl.backgroundColor = _layPico.backgroundColor;
        lbl.numberOfLines = 0;
        lbl.textAlignment = NSTextAlignmentCenter;
        lbl.font = [UIFont fontWithName:lbl.font.fontName size:12];
        lbl.text = [NSString stringWithFormat:@"MATCH %d", i + 1];
        [_lblMatches addObject:lbl];
        [self.view addSubview:lbl];
    }

    _scroll = UIScrollView.alloc.init;
    _scroll.translatesAutoresizingMaskIntoConstraints = NO;
    [self.view addSubview:_scroll];

    _scrollContent = UIView.alloc.init;
    _scrollContent.translatesAutoresizingMaskIntoConstraints = NO;
    [_scroll addSubview:_scrollContent];

    _lblLog = UILabel.alloc.init;
    _lblLog.translatesAutoresizingMaskIntoConstraints = NO;
    _lblLog.numberOfLines = 0;
    _lblLog.text = @"";
    [_scrollContent addSubview:_lblLog];

    _btnScan = [UIButton buttonWithType:UIButtonTypeSystem];
    _btnScan.translatesAutoresizingMaskIntoConstraints = NO;
    _btnScan.contentEdgeInsets = UIEdgeInsetsMake(20, 20, 20, 20);
    [_btnScan setTitle:@"SCAN" forState:UIControlStateNormal];
    [_btnScan addTarget:self action:@selector(onScanTap) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:_btnScan];

    _btnCalibrate = [UIButton buttonWithType:UIButtonTypeSystem];
    _btnCalibrate.translatesAutoresizingMaskIntoConstraints = NO;
    _btnCalibrate.contentEdgeInsets = UIEdgeInsetsMake(20, 20, 20, 20);
    [_btnCalibrate setTitle:@"CALIBRATE" forState:UIControlStateNormal];
    [_btnCalibrate addTarget:self action:@selector(onCalibrateTap) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:_btnCalibrate];

    _btnConnect = [UIButton buttonWithType:UIButtonTypeSystem];
    _btnConnect.translatesAutoresizingMaskIntoConstraints = NO;
    _btnConnect.contentEdgeInsets = UIEdgeInsetsMake(20, 20, 20, 20);
    [_btnConnect setTitle:@"CONNECT" forState:UIControlStateNormal];
    [_btnConnect addTarget:self action:@selector(onConnectTap) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:_btnConnect];

    _btnDisconnect = [UIButton buttonWithType:UIButtonTypeSystem];
    _btnDisconnect.translatesAutoresizingMaskIntoConstraints = NO;
    _btnDisconnect.contentEdgeInsets = UIEdgeInsetsMake(20, 20, 20, 20);
    [_btnDisconnect setTitle:@"DISCONNECT" forState:UIControlStateNormal];
    [_btnDisconnect addTarget:self action:@selector(onDisconnectTap) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:_btnDisconnect];


    // Setup constraints
    [_layPico.heightAnchor constraintEqualToConstant:60].active = YES;
    [_layPico.topAnchor constraintEqualToAnchor:self.topLayoutGuide.bottomAnchor].active = YES;
    [_layPico.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor].active = YES;
    [_layPico.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor].active = YES;

    [_lblPico.centerYAnchor constraintEqualToAnchor:_layPico.centerYAnchor].active = YES;
    [_lblPico.leadingAnchor constraintEqualToAnchor:_layPico.leadingAnchor constant:15].active = YES;
    [_lblPico.trailingAnchor constraintEqualToAnchor:_layPico.trailingAnchor constant:-15].active = YES;

    [_lblScan.heightAnchor constraintEqualToConstant:60].active = YES;
    [_lblScan.topAnchor constraintEqualToAnchor:_layPico.bottomAnchor].active = YES;
    [_lblScan.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor].active = YES;

    [[_lblMatches[0] widthAnchor] constraintEqualToAnchor:_lblScan.widthAnchor].active = YES;
    [[_lblMatches[0] topAnchor] constraintEqualToAnchor:_lblScan.topAnchor].active = YES;
    [[_lblMatches[0] bottomAnchor] constraintEqualToAnchor:_lblScan.bottomAnchor].active = YES;
    [[_lblMatches[0] leadingAnchor] constraintEqualToAnchor:_lblScan.trailingAnchor].active = YES;

    [[_lblMatches[1] widthAnchor] constraintEqualToAnchor:_lblScan.widthAnchor].active = YES;
    [[_lblMatches[1] topAnchor] constraintEqualToAnchor:_lblScan.topAnchor].active = YES;
    [[_lblMatches[1] bottomAnchor] constraintEqualToAnchor:_lblScan.bottomAnchor].active = YES;
    [[_lblMatches[1] leadingAnchor] constraintEqualToAnchor:[_lblMatches[0] trailingAnchor]].active = YES;

    [[_lblMatches[2] widthAnchor] constraintEqualToAnchor:_lblScan.widthAnchor].active = YES;
    [[_lblMatches[2] topAnchor] constraintEqualToAnchor:_lblScan.topAnchor].active = YES;
    [[_lblMatches[2] bottomAnchor] constraintEqualToAnchor:_lblScan.bottomAnchor].active = YES;
    [[_lblMatches[2] leadingAnchor] constraintEqualToAnchor:[_lblMatches[1] trailingAnchor]].active = YES;
    [[_lblMatches[2] trailingAnchor] constraintEqualToAnchor:self.view.trailingAnchor].active = YES;

    [_scroll.topAnchor constraintEqualToAnchor:_lblScan.bottomAnchor].active = YES;
    [_scroll.bottomAnchor constraintEqualToAnchor:_btnScan.topAnchor].active = YES;
    [_scroll.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor].active = YES;
    [_scroll.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor].active = YES;

    [_scrollContent.widthAnchor constraintEqualToAnchor:_scroll.widthAnchor].active = YES;
    [_scrollContent.topAnchor constraintEqualToAnchor:_scroll.topAnchor].active = YES;
    [_scrollContent.bottomAnchor constraintEqualToAnchor:_scroll.bottomAnchor].active = YES;
    [_scrollContent.leadingAnchor constraintEqualToAnchor:_scroll.leadingAnchor].active = YES;
    [_scrollContent.trailingAnchor constraintEqualToAnchor:_scroll.trailingAnchor].active = YES;

    [_lblLog.topAnchor constraintEqualToAnchor:_scrollContent.topAnchor constant:15].active = YES;
    [_lblLog.bottomAnchor constraintEqualToAnchor:_scrollContent.bottomAnchor constant:-15].active = YES;
    [_lblLog.leadingAnchor constraintEqualToAnchor:_scrollContent.leadingAnchor constant:15].active = YES;
    [_lblLog.trailingAnchor constraintEqualToAnchor:_scrollContent.trailingAnchor constant:-15].active = YES;

    [_btnScan.widthAnchor constraintEqualToAnchor:_btnCalibrate.widthAnchor].active = YES;
    [_btnScan.widthAnchor constraintEqualToAnchor:_btnConnect.widthAnchor].active = YES;
    [_btnScan.widthAnchor constraintEqualToAnchor:_btnDisconnect.widthAnchor].active = YES;

    [_btnScan.bottomAnchor constraintEqualToAnchor:_btnConnect.topAnchor constant:-5].active = YES;
    [_btnScan.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:5].active = YES;
    [_btnScan.trailingAnchor constraintEqualToAnchor:_btnCalibrate.leadingAnchor constant:-5].active = YES;

    [_btnCalibrate.bottomAnchor constraintEqualToAnchor:_btnScan.bottomAnchor].active = YES;
    [_btnCalibrate.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-5].active = YES;

    [_btnConnect.bottomAnchor constraintEqualToAnchor:self.view.bottomAnchor constant:-5].active = YES;
    [_btnConnect.leadingAnchor constraintEqualToAnchor:_btnScan.leadingAnchor].active = YES;

    [_btnDisconnect.bottomAnchor constraintEqualToAnchor:_btnConnect.bottomAnchor].active = YES;
    [_btnDisconnect.trailingAnchor constraintEqualToAnchor:_btnCalibrate.trailingAnchor].active = YES;
}


- (void)log:(NSString *)text
{
    _lblLog.text = [NSString stringWithFormat:@"%@%@\n", _lblLog.text, text];

    if (_scroll.contentSize.height > _scroll.bounds.size.height)
    {
        CGPoint offset = CGPointMake(0, _scroll.contentSize.height - _scroll.bounds.size.height + 30);
        [_scroll setContentOffset:offset animated:YES];
    }
}


- (NSArray *)getSampleColors
{
    return @[
        [CUSwatch.alloc initWithName:@"Orange" code:@"A" lab:[CULAB.alloc initWithL:62.966542929419 a:58.1009869314696 b:66.6022417932114]],
        [CUSwatch.alloc initWithName:@"Red" code:@"B" lab:[CULAB.alloc initWithL:44.9512479211058 a:42.9550636736008 b:34.5789150804718]],
        [CUSwatch.alloc initWithName:@"Blue" code:@"C" lab:[CULAB.alloc initWithL:46.4326794509501 a:-4.91999247990771 b:-46.1209053819398]],
        [CUSwatch.alloc initWithName:@"Green" code:@"D" lab:[CULAB.alloc initWithL:69.0594692695158 a:-32.2304850310249 b:53.5603525426951]],
        [CUSwatch.alloc initWithName:@"Grey" code:@"E" lab:[CULAB.alloc initWithL:31.5130852290379 a:2.28639746047626 b:0.592541444296391]],
        [CUSwatch.alloc initWithName:@"Black" code:@"F" lab:[CULAB.alloc initWithL:25.1042112878437 a:0.292026771056303 b:2.40698876603797]],
        [CUSwatch.alloc initWithName:@"Purple" code:@"G" lab:[CULAB.alloc initWithL:38.3519350654045 a:12.1653856460046 b:-11.7184394987654]],
        [CUSwatch.alloc initWithName:@"Brown" code:@"H" lab:[CULAB.alloc initWithL:33.1952918599344 a:6.05497130641686 b:4.63902305326908]],
    ];
}


/**
 * CUPicoManagerDelegate events.
 */

- (void)onConnectSuccess:(CUPico *)pico
{
    _pico = pico;

    [self log:@"Pico connected"];
    _lblPico.text = [NSString stringWithFormat:@"%@\n%@", _pico.name, _pico.serial];

    _pico.delegate = self;

    [_pico sendBatteryLevelRequest];
    [_pico sendBatteryStatusRequest];
}

- (void)onConnectFail:(NSError *)error
{
    [self log:[NSString stringWithFormat:@"Failed to connect to Pico: %@", error.description]];
}


/**
 * CUPicoDelegate events.
 */

- (void)onDisconnect:(CUPico *)pico error:(NSError *)error
{
    [self log:@"Pico disconnected"];
    _lblPico.text = @"Pico disconnected";
}

- (void)onFetchLabData:(CUPico *)pico lab:(CULAB *)lab
{
    [self log:[NSString stringWithFormat:@"Received LAB: %@", lab.toString]];

    // Convert lab to rgb and display it.
    _lblScan.backgroundColor = lab.color;

    // Find matches and display them.
    NSArray *swatches = self.getSampleColors;
    NSArray *matches = [CUSwatchMatcher getMatches:lab swatches:swatches numMatches:3];

    for (NSInteger i = 0; i < matches.count; i++)
    {
        CUMatch *match = matches[i];

        UILabel *lbl = _lblMatches[i];
        lbl.backgroundColor = match.swatch.lab.color;
        lbl.text = [NSString stringWithFormat:@"MATCH %d\n%@\ndE %.2f", i + 1, match.swatch.name, match.distance];
    }
}

- (void)onCalibrationComplete:(CUPico *)pico success:(BOOL)success
{
    NSString *result = success ? @"success" : @"failure";
    [self log:[NSString stringWithFormat:@"Calibration complete: %@", result]];
}

- (void)onFetchBatteryLevel:(CUPico *)pico level:(NSInteger)level
{
    [self log:[NSString stringWithFormat:@"Battery level: %d", level]];
}

- (void)onFetchBatteryStatus:(CUPico *)pico status:(CUBatteryStatus)status
{
    NSString *str = @"Unknown";
    switch (status)
    {
        case CUBatteryStatusNotCharging:
            str = @"NotCharging";
            break;
        case CUBatteryStatusCharging:
            str = @"Charging";
            break;
        case CUBatteryStatusCharged:
            str = @"Charged";
            break;
    }

    [self log:[NSString stringWithFormat:@"Battery status: %@", str]];
}


/**
 * UI events.
 */

- (void)onScanTap
{
    [_pico sendLabDataRequest];
}

- (void)onCalibrateTap
{
    [_pico sendCalibrationRequest];
}

- (void)onConnectTap
{
    [_picoConnector connect];
}

- (void)onDisconnectTap
{
    if (_pico != nil)
    {
        [_pico disconnect];
        _pico = nil;
    }
}

@end
