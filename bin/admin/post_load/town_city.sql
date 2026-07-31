-- Run by update_district_geometry.sh after it reloads districts.town_city.
--
-- abbrev holds custom 6-character streetfile codes.
-- Three codes are deliberately assigned twice. Albion and Lewis each name two towns,
-- while Palm Tree split off from Monroe.

ALTER TABLE districts.town_city ADD COLUMN IF NOT EXISTS abbrev character varying(6);

WITH codes (gnis_id, abbrev) AS (VALUES
    ('978655', 'ADAMS'),     -- Adams, Jefferson
    ('978656', 'ADDISO'),    -- Addison, Steuben
    ('978657', 'AFTON'),     -- Afton, Chenango
    ('978658', 'ALABAM'),    -- Alabama, Genesee
    ('978659', '-ALBAN'),    -- Albany, Albany
    ('978660', 'ALBION'),    -- Albion, Orleans
    ('978661', 'ALBION'),    -- Albion, Oswego
    ('978662', 'ALDEN'),     -- Alden, Erie
    ('978663', 'ALEXAN'),    -- Alexander, Genesee
    ('978664', 'ALEXAD'),    -- Alexandria, Jefferson
    ('978665', 'ALFRED'),    -- Alfred, Allegany
    ('978666', 'ALLEGA'),    -- Allegany, Cattaraugus
    ('978668', 'ALLEN'),     -- Allen, Allegany
    ('978669', 'ALMA'),      -- Alma, Allegany
    ('978670', 'ALMOND'),    -- Almond, Allegany
    ('978672', 'ALTONA'),    -- Altona, Clinton
    ('978673', 'AMBOY'),     -- Amboy, Oswego
    ('978674', 'AMENIA'),    -- Amenia, Dutchess
    ('978675', 'AMHERS'),    -- Amherst, Erie
    ('978676', 'AMITY'),     -- Amity, Allegany
    ('978678', 'AMSTER'),    -- Amsterdam, Montgomery
    ('978677', '-AMSTE'),    -- Amsterdam, Montgomery
    ('978679', 'ANCRAM'),    -- Ancram, Columbia
    ('978680', 'ANDES'),     -- Andes, Delaware
    ('978681', 'ANDOVE'),    -- Andover, Allegany
    ('978682', 'ANGELI'),    -- Angelica, Allegany
    ('978683', 'ANNSVI'),    -- Annsville, Oneida
    ('978684', 'ANTWER'),    -- Antwerp, Jefferson
    ('978685', 'ARCADE'),    -- Arcade, Wyoming
    ('978686', 'ARCADI'),    -- Arcadia, Wayne
    ('978687', 'ARGYLE'),    -- Argyle, Washington
    ('978688', 'ARIETT'),    -- Arietta, Hamilton
    ('978689', 'ARKWRI'),    -- Arkwright, Chautauqua
    ('978690', 'ASHFOR'),    -- Ashford, Cattaraugus
    ('978691', 'ASHLAN'),    -- Ashland, Chemung
    ('978692', 'ASHLAD'),    -- Ashland, Greene
    ('978693', 'ATHENS'),    -- Athens, Greene
    ('978694', 'ATTICA'),    -- Attica, Wyoming
    ('978699', 'AUSABL'),    -- AuSable, Clinton
    ('978695', '-AUBUR'),    -- Auburn, Cayuga
    ('978696', 'AUGUST'),    -- Augusta, Oneida
    ('978697', 'AURELI'),    -- Aurelius, Cayuga
    ('978698', 'AURORA'),    -- Aurora, Erie
    ('978700', 'AUSTER'),    -- Austerlitz, Columbia
    ('978701', 'AVA'),       -- Ava, Oneida
    ('978702', 'AVOCA'),     -- Avoca, Steuben
    ('978703', 'AVON'),      -- Avon, Livingston
    ('978704', 'BABYLO'),    -- Babylon, Suffolk
    ('978705', 'BAINBR'),    -- Bainbridge, Chenango
    ('978706', 'BALDWI'),    -- Baldwin, Chemung
    ('978707', 'BALLST'),    -- Ballston, Saratoga
    ('978708', 'BANGOR'),    -- Bangor, Franklin
    ('978709', 'BARKER'),    -- Barker, Broome
    ('978710', 'BARRE'),     -- Barre, Orleans
    ('978711', 'BARRIN'),    -- Barrington, Yates
    ('978712', 'BARTON'),    -- Barton, Tioga
    ('978713', '-BATAV'),    -- Batavia, Genesee
    ('978714', 'BATAVI'),    -- Batavia, Genesee
    ('978715', 'BATH'),      -- Bath, Steuben
    ('978716', '-BEACO'),    -- Beacon, Dutchess
    ('978717', 'BEDFOR'),    -- Bedford, Westchester
    ('978718', 'BEEKMA'),    -- Beekman, Dutchess
    ('978719', 'BEEKMT'),    -- Beekmantown, Clinton
    ('978720', 'BELFAS'),    -- Belfast, Allegany
    ('978721', 'BELLMO'),    -- Bellmont, Franklin
    ('978722', 'BENNIN'),    -- Bennington, Wyoming
    ('978723', 'BENSON'),    -- Benson, Hamilton
    ('978724', 'BENTON'),    -- Benton, Yates
    ('978725', 'BERGEN'),    -- Bergen, Genesee
    ('978726', 'BERKSH'),    -- Berkshire, Tioga
    ('978727', 'BERLIN'),    -- Berlin, Rensselaer
    ('978728', 'BERNE'),     -- Berne, Albany
    ('978729', 'BETHAN'),    -- Bethany, Genesee
    ('978730', 'BETHEL'),    -- Bethel, Sullivan
    ('978731', 'BETHLE'),    -- Bethlehem, Albany
    ('978732', 'BIG FL'),    -- Big Flats, Chemung
    ('978733', '-BINGH'),    -- Binghamton, Broome
    ('978734', 'BINGHA'),    -- Binghamton, Broome
    ('978735', 'BIRDSA'),    -- Birdsall, Allegany
    ('978736', 'BLACK'),     -- Black Brook, Clinton
    ('978737', 'BLEECK'),    -- Bleecker, Fulton
    ('978738', 'BLENHE'),    -- Blenheim, Schoharie
    ('978739', 'BLOOMI'),    -- Blooming Grove, Orange
    ('978740', 'BOLIVA'),    -- Bolivar, Allegany
    ('978741', 'BOLTON'),    -- Bolton, Warren
    ('978742', 'BOMBAY'),    -- Bombay, Franklin
    ('978743', 'BOONVI'),    -- Boonville, Oneida
    ('978744', 'BOSTON'),    -- Boston, Erie
    ('978745', 'BOVINA'),    -- Bovina, Delaware
    ('978746', 'BOYLST'),    -- Boylston, Oswego
    ('978747', 'BRADFO'),    -- Bradford, Steuben
    ('978748', 'BRANDO'),    -- Brandon, Franklin
    ('978749', 'BRANT'),     -- Brant, Erie
    ('978750', 'BRASHE'),    -- Brasher, St. Lawrence
    ('978751', 'BRIDGE'),    -- Bridgewater, Oneida
    ('978752', 'BRIGHN'),    -- Brighton, Franklin
    ('978753', 'BRIGHT'),    -- Brighton, Monroe
    ('978754', 'BRISTO'),    -- Bristol, Ontario
    ('978755', 'BROADA'),    -- Broadalbin, Fulton
    ('978757', 'BROOKF'),    -- Brookfield, Madison
    ('978758', 'BROOKH'),    -- Brookhaven, Suffolk
    ('978760', 'BROOME'),    -- Broome, Schoharie
    ('978761', 'BROWNV'),    -- Brownville, Jefferson
    ('978762', 'BRUNSW'),    -- Brunswick, Rensselaer
    ('978763', 'BRUTUS'),    -- Brutus, Cayuga
    ('978764', '-BUFFA'),    -- Buffalo, Erie
    ('978765', 'BURKE'),     -- Burke, Franklin
    ('978766', 'BURLIN'),    -- Burlington, Otsego
    ('978767', 'BURNS'),     -- Burns, Allegany
    ('978768', 'BUSTI'),     -- Busti, Chautauqua
    ('978769', 'BUTLER'),    -- Butler, Wayne
    ('978770', 'BUTTER'),    -- Butternuts, Otsego
    ('978771', 'BYRON'),     -- Byron, Genesee
    ('978772', 'CAIRO'),     -- Cairo, Greene
    ('978773', 'CALEDO'),    -- Caledonia, Livingston
    ('978774', 'CALLIC'),    -- Callicoon, Sullivan
    ('978775', 'CAMBRI'),    -- Cambria, Niagara
    ('978776', 'CAMBRD'),    -- Cambridge, Washington
    ('978777', 'CAMDEN'),    -- Camden, Oneida
    ('978778', 'CAMERO'),    -- Cameron, Steuben
    ('978779', 'CAMILL'),    -- Camillus, Onondaga
    ('978780', 'CAMPBE'),    -- Campbell, Steuben
    ('978781', 'CANAAN'),    -- Canaan, Columbia
    ('978782', 'CANADI'),    -- Canadice, Ontario
    ('978783', 'CANAJO'),    -- Canajoharie, Montgomery
    ('978785', 'CANAND'),    -- Canandaigua, Ontario
    ('978784', '-CANAN'),    -- Canandaigua, Ontario
    ('978786', 'CANDOR'),    -- Candor, Tioga
    ('978787', 'CANEAD'),    -- Caneadea, Allegany
    ('978788', 'CANIST'),    -- Canisteo, Steuben
    ('978789', 'CANTON'),    -- Canton, St. Lawrence
    ('978790', 'CAPE V'),    -- Cape Vincent, Jefferson
    ('978791', 'CARLIS'),    -- Carlisle, Schoharie
    ('978792', 'CARLTO'),    -- Carlton, Orleans
    ('978793', 'CARMEL'),    -- Carmel, Putnam
    ('978794', 'CAROGA'),    -- Caroga, Fulton
    ('978795', 'CAROLI'),    -- Caroline, Tompkins
    ('978796', 'CARROL'),    -- Carroll, Chautauqua
    ('978797', 'CARROT'),    -- Carrollton, Cattaraugus
    ('978798', 'CASTIL'),    -- Castile, Wyoming
    ('978799', 'CATHAR'),    -- Catharine, Schuyler
    ('978800', 'CATLIN'),    -- Catlin, Chemung
    ('978801', 'CATO'),      -- Cato, Cayuga
    ('978802', 'CATON'),     -- Caton, Steuben
    ('978803', 'CATSKI'),    -- Catskill, Greene
    ('978805', 'CAYUTA'),    -- Cayuta, Schuyler
    ('978806', 'CAZENO'),    -- Cazenovia, Madison
    ('978807', 'CENTER'),    -- Centerville, Allegany
    ('978808', 'CHAMPI'),    -- Champion, Jefferson
    ('978809', 'CHAMPL'),    -- Champlain, Clinton
    ('978810', 'CHARLE'),    -- Charleston, Montgomery
    ('978811', 'CHARLO'),    -- Charlotte, Chautauqua
    ('978812', 'CHARLT'),    -- Charlton, Saratoga
    ('978813', 'CHATEA'),    -- Chateaugay, Franklin
    ('978814', 'CHATHA'),    -- Chatham, Columbia
    ('978815', 'CHAUTA'),    -- Chautauqua, Chautauqua
    ('978816', 'CHAZY'),     -- Chazy, Clinton
    ('978817', 'CHEEKT'),    -- Cheektowaga, Erie
    ('978818', 'CHEMUN'),    -- Chemung, Chemung
    ('978819', 'CHENAN'),    -- Chenango, Broome
    ('978820', 'CHERRY'),    -- Cherry Creek, Chautauqua
    ('978821', 'CHERRV'),    -- Cherry Valley, Otsego
    ('978822', 'CHESTR'),    -- Chester, Orange
    ('978823', 'CHESTE'),    -- Chester, Warren
    ('978824', 'CHESTF'),    -- Chesterfield, Essex
    ('978825', 'CHILI'),     -- Chili, Monroe
    ('978826', 'CICERO'),    -- Cicero, Onondaga
    ('978827', 'CINCIN'),    -- Cincinnatus, Cortland
    ('978828', 'CLARE'),     -- Clare, St. Lawrence
    ('978829', 'CLAREN'),    -- Clarence, Erie
    ('978830', 'CLARED'),    -- Clarendon, Orleans
    ('978831', 'CLARKS'),    -- Clarkson, Monroe
    ('978832', 'CLARKT'),    -- Clarkstown, Rockland
    ('978833', 'CLARKV'),    -- Clarksville, Allegany
    ('978834', 'CLAVER'),    -- Claverack, Columbia
    ('978835', 'CLAY'),      -- Clay, Onondaga
    ('978836', 'CLAYTO'),    -- Clayton, Jefferson
    ('978837', 'CLERMO'),    -- Clermont, Columbia
    ('978838', 'CLIFTO'),    -- Clifton, St. Lawrence
    ('978839', 'CLIFTP'),    -- Clifton Park, Saratoga
    ('978840', 'CLINTN'),    -- Clinton, Clinton
    ('978841', 'CLINTO'),    -- Clinton, Dutchess
    ('978842', 'CLYMER'),    -- Clymer, Chautauqua
    ('978843', 'COBLES'),    -- Cobleskill, Schoharie
    ('978844', 'COCHEC'),    -- Cochecton, Sullivan
    ('978845', 'COEYMA'),    -- Coeymans, Albany
    ('978846', 'COHOCT'),    -- Cohocton, Steuben
    ('978847', '-COHOE'),    -- Cohoes, Albany
    ('978848', 'COLCHE'),    -- Colchester, Delaware
    ('978849', 'COLDEN'),    -- Colden, Erie
    ('978850', 'COLDSP'),    -- Coldspring, Cattaraugus
    ('978851', 'COLESV'),    -- Colesville, Broome
    ('978852', 'COLLIN'),    -- Collins, Erie
    ('978853', 'COLONI'),    -- Colonie, Albany
    ('978854', 'COLTON'),    -- Colton, St. Lawrence
    ('978855', 'COLUMB'),    -- Columbia, Herkimer
    ('978856', 'COLUMS'),    -- Columbus, Chenango
    ('978857', 'CONCOR'),    -- Concord, Erie
    ('978858', 'CONESU'),    -- Conesus, Livingston
    ('978859', 'CONESV'),    -- Conesville, Schoharie
    ('978860', 'CONEWA'),    -- Conewango, Cattaraugus
    ('978861', 'CONKLI'),    -- Conklin, Broome
    ('978862', 'CONQUE'),    -- Conquest, Cayuga
    ('978863', 'CONSTB'),    -- Constable, Franklin
    ('978864', 'CONSTA'),    -- Constantia, Oswego
    ('978865', 'COPAKE'),    -- Copake, Columbia
    ('978866', 'CORINT'),    -- Corinth, Saratoga
    ('978867', '-CORNI'),    -- Corning, Steuben
    ('978868', 'CORNIN'),    -- Corning, Steuben
    ('978869', 'CORNWA'),    -- Cornwall, Orange
    ('978870', '-CORTL'),    -- Cortland, Cortland
    ('978871', 'CORTLA'),    -- Cortlandt, Westchester
    ('978872', 'CORTLV'),    -- Cortlandville, Cortland
    ('978873', 'COVENT'),    -- Coventry, Chenango
    ('978874', 'COVERT'),    -- Covert, Seneca
    ('978875', 'COVING'),    -- Covington, Wyoming
    ('978876', 'COXSAC'),    -- Coxsackie, Greene
    ('978877', 'CRAWFO'),    -- Crawford, Orange
    ('978878', 'CROGHA'),    -- Croghan, Lewis
    ('978879', 'CROWN'),     -- Crown Point, Essex
    ('978880', 'CUBA'),      -- Cuba, Allegany
    ('978881', 'CUYLER'),    -- Cuyler, Cortland
    ('978882', 'DANBY'),     -- Danby, Tompkins
    ('978883', 'DANNEM'),    -- Dannemora, Clinton
    ('978884', 'DANSVI'),    -- Dansville, Steuben
    ('978885', 'DANUBE'),    -- Danube, Herkimer
    ('978886', 'DARIEN'),    -- Darien, Genesee
    ('978887', 'DAVENP'),    -- Davenport, Delaware
    ('978888', 'DAY'),       -- Day, Saratoga
    ('978889', 'DAYTON'),    -- Dayton, Cattaraugus
    ('978893', 'DEKALB'),    -- De Kalb, St. Lawrence
    ('978898', 'DEPEYS'),    -- De Peyster, St. Lawrence
    ('978901', 'DEWITT'),    -- De Witt, Onondaga
    ('978900', 'DERUYT'),    -- DeRuyter, Madison
    ('978890', 'DECATU'),    -- Decatur, Otsego
    ('978891', 'DEERFI'),    -- Deerfield, Oneida
    ('978892', 'DEERPA'),    -- Deerpark, Orange
    ('978894', 'DELAWA'),    -- Delaware, Sullivan
    ('978895', 'DELHI'),     -- Delhi, Delaware
    ('978896', 'DENMAR'),    -- Denmark, Lewis
    ('978897', 'DENNIN'),    -- Denning, Ulster
    ('978899', 'DEPOSI'),    -- Deposit, Delaware
    ('978902', 'DIANA'),     -- Diana, Lewis
    ('978903', 'DICKIN'),    -- Dickinson, Broome
    ('978904', 'DICKIS'),    -- Dickinson, Franklin
    ('978905', 'DIX'),       -- Dix, Schuyler
    ('978906', 'DOVER'),     -- Dover, Dutchess
    ('978907', 'DRESDE'),    -- Dresden, Washington
    ('978908', 'DRYDEN'),    -- Dryden, Tompkins
    ('978909', 'DUANE'),     -- Duane, Franklin
    ('978910', 'DUANES'),    -- Duanesburg, Schenectady
    ('978912', 'DUNKIR'),    -- Dunkirk, Chautauqua
    ('978911', '-DUNKI'),    -- Dunkirk, Chautauqua
    ('978913', 'DURHAM'),    -- Durham, Greene
    ('978914', 'EAGLE'),     -- Eagle, Wyoming
    ('978915', 'E BLOO'),    -- East Bloomfield, Ontario
    ('978917', 'E FISH'),    -- East Fishkill, Dutchess
    ('978918', 'E GREE'),    -- East Greenbush, Rensselaer
    ('978919', 'E HAMP'),    -- East Hampton, Suffolk
    ('978921', 'E OTTO'),    -- East Otto, Cattaraugus
    ('978922', 'E ROCH'),    -- East Rochester, Monroe
    ('978916', 'EASTCH'),    -- Eastchester, Westchester
    ('978920', 'EASTON'),    -- Easton, Washington
    ('978923', 'EATON'),     -- Eaton, Madison
    ('978924', 'EDEN'),      -- Eden, Erie
    ('978925', 'EDINBU'),    -- Edinburg, Saratoga
    ('978926', 'EDMEST'),    -- Edmeston, Otsego
    ('978927', 'EDWARD'),    -- Edwards, St. Lawrence
    ('978928', 'ELBA'),      -- Elba, Genesee
    ('978929', 'ELBRID'),    -- Elbridge, Onondaga
    ('978930', 'ELIZAB'),    -- Elizabethtown, Essex
    ('978931', 'ELLENB'),    -- Ellenburg, Clinton
    ('978932', 'ELLERY'),    -- Ellery, Chautauqua
    ('978933', 'ELLICO'),    -- Ellicott, Chautauqua
    ('978934', 'ELLICV'),    -- Ellicottville, Cattaraugus
    ('978935', 'ELLING'),    -- Ellington, Chautauqua
    ('978936', 'ELLISB'),    -- Ellisburg, Jefferson
    ('978937', 'ELMA'),      -- Elma, Erie
    ('978939', 'ELMIRA'),    -- Elmira, Chemung
    ('978938', '-ELMIR'),    -- Elmira, Chemung
    ('978940', 'ENFIEL'),    -- Enfield, Tompkins
    ('978941', 'EPHRAT'),    -- Ephratah, Fulton
    ('978942', 'ERIN'),      -- Erin, Chemung
    ('978943', 'ERWIN'),     -- Erwin, Steuben
    ('978944', 'ESOPUS'),    -- Esopus, Ulster
    ('978945', 'ESPERA'),    -- Esperance, Schoharie
    ('978946', 'ESSEX'),     -- Essex, Essex
    ('978947', 'EVANS'),     -- Evans, Erie
    ('978948', 'EXETER'),    -- Exeter, Otsego
    ('978949', 'FABIUS'),    -- Fabius, Onondaga
    ('978950', 'FAIRFI'),    -- Fairfield, Herkimer
    ('978951', 'FALLSB'),    -- Fallsburg, Sullivan
    ('978952', 'FARMER'),    -- Farmersville, Cattaraugus
    ('978953', 'FARMIN'),    -- Farmington, Ontario
    ('978954', 'FAYETT'),    -- Fayette, Seneca
    ('978955', 'FENNER'),    -- Fenner, Madison
    ('978956', 'FENTON'),    -- Fenton, Broome
    ('978957', 'FINE'),      -- Fine, St. Lawrence
    ('978958', 'FISHKI'),    -- Fishkill, Dutchess
    ('978959', 'FLEMIN'),    -- Fleming, Cayuga
    ('978960', 'FLOREN'),    -- Florence, Oneida
    ('978961', 'FLORID'),    -- Florida, Montgomery
    ('978962', 'FLOYD'),     -- Floyd, Oneida
    ('978963', 'FORESB'),    -- Forestburgh, Sullivan
    ('978964', 'FOREST'),    -- Forestport, Oneida
    ('978965', 'FT ANN'),    -- Fort Ann, Washington
    ('978966', 'FT COV'),    -- Fort Covington, Franklin
    ('978967', 'FT EDW'),    -- Fort Edward, Washington
    ('978968', 'FOWLER'),    -- Fowler, St. Lawrence
    ('978969', 'FRANKF'),    -- Frankfort, Herkimer
    ('978970', 'FRANKN'),    -- Franklin, Delaware
    ('978971', 'FRANKL'),    -- Franklin, Franklin
    ('978972', 'FRANKV'),    -- Franklinville, Cattaraugus
    ('978973', 'FREEDO'),    -- Freedom, Cattaraugus
    ('978974', 'FREETO'),    -- Freetown, Cortland
    ('978975', 'FREMOT'),    -- Fremont, Steuben
    ('978976', 'FREMON'),    -- Fremont, Sullivan
    ('978977', 'FRENCH'),    -- French Creek, Chautauqua
    ('978978', 'FRIEND'),    -- Friendship, Allegany
    ('978979', '-FULTO'),    -- Fulton, Oswego
    ('978980', 'FULTON'),    -- Fulton, Schoharie
    ('978981', 'GAINES'),    -- Gaines, Orleans
    ('978982', 'GAINEV'),    -- Gainesville, Wyoming
    ('978983', 'GALEN'),     -- Galen, Wayne
    ('978984', 'GALLAT'),    -- Gallatin, Columbia
    ('978985', 'GALWAY'),    -- Galway, Saratoga
    ('978986', 'GARDIN'),    -- Gardiner, Ulster
    ('978987', 'GATES'),     -- Gates, Monroe
    ('978988', 'GEDDES'),    -- Geddes, Onondaga
    ('978989', 'GENESE'),    -- Genesee, Allegany
    ('978990', 'GENESF'),    -- Genesee Falls, Wyoming
    ('978991', 'GENESO'),    -- Geneseo, Livingston
    ('978993', 'GENEVA'),    -- Geneva, Ontario
    ('978992', '-GENEV'),    -- Geneva, Ontario, Seneca
    ('978994', 'GENOA'),     -- Genoa, Cayuga
    ('978995', 'GEORGE'),    -- Georgetown, Madison
    ('978996', 'GERMAN'),    -- German, Chenango
    ('978997', 'GERMAF'),    -- German Flatts, Herkimer
    ('978998', 'GERMAT'),    -- Germantown, Columbia
    ('978999', 'GERRY'),     -- Gerry, Chautauqua
    ('979000', 'GHENT'),     -- Ghent, Columbia
    ('979001', 'GILBOA'),    -- Gilboa, Schoharie
    ('979002', 'GLEN'),      -- Glen, Montgomery
    ('979003', '-GLEN'),     -- Glen Cove, Nassau
    ('979004', '-GLENS'),    -- Glens Falls, Warren
    ('979005', 'GLENVI'),    -- Glenville, Schenectady
    ('979006', '-GLOVE'),    -- Gloversville, Fulton
    ('979007', 'GORHAM'),    -- Gorham, Ontario
    ('979008', 'GOSHEN'),    -- Goshen, Orange
    ('979009', 'GOUVER'),    -- Gouverneur, St. Lawrence
    ('979010', 'GRAFTO'),    -- Grafton, Rensselaer
    ('979011', 'GRANBY'),    -- Granby, Oswego
    ('979012', 'GRAND'),     -- Grand Island, Erie
    ('979013', 'GRANGE'),    -- Granger, Allegany
    ('979014', 'GRANVI'),    -- Granville, Washington
    ('979015', 'GREAT'),     -- Great Valley, Cattaraugus
    ('979016', 'GREECE'),    -- Greece, Monroe
    ('979020', 'GREEN'),     -- Green Island, Albany
    ('979017', 'GREENB'),    -- Greenburgh, Westchester
    ('979018', 'GREENE'),    -- Greene, Chenango
    ('979019', 'GREENF'),    -- Greenfield, Saratoga
    ('979021', 'GREENP'),    -- Greenport, Columbia
    ('979022', 'GREENV'),    -- Greenville, Greene
    ('979023', 'GREENO'),    -- Greenville, Orange
    ('979024', 'GREENW'),    -- Greenwich, Washington
    ('979025', 'GREEND'),    -- Greenwood, Steuben
    ('979026', 'GREIG'),     -- Greig, Lewis
    ('979027', 'GROTON'),    -- Groton, Tompkins
    ('979028', 'GROVE'),     -- Grove, Allegany
    ('979029', 'GROVEL'),    -- Groveland, Livingston
    ('979030', 'GUILDE'),    -- Guilderland, Albany
    ('979031', 'GUILFO'),    -- Guilford, Chenango
    ('979032', 'HADLEY'),    -- Hadley, Saratoga
    ('979033', 'HAGUE'),     -- Hague, Warren
    ('979034', 'HALCOT'),    -- Halcott, Greene
    ('979035', 'HALFMO'),    -- Halfmoon, Saratoga
    ('979036', 'HAMBUR'),    -- Hamburg, Erie
    ('979037', 'HAMDEN'),    -- Hamden, Delaware
    ('979038', 'HAMILT'),    -- Hamilton, Madison
    ('979039', 'HAMLIN'),    -- Hamlin, Monroe
    ('979040', 'HAMMON'),    -- Hammond, St. Lawrence
    ('979041', 'HAMPTO'),    -- Hampton, Washington
    ('979042', 'HAMPTB'),    -- Hamptonburgh, Orange
    ('979043', 'HANCOC'),    -- Hancock, Delaware
    ('979044', 'HANNIB'),    -- Hannibal, Oswego
    ('979045', 'HANOVE'),    -- Hanover, Chautauqua
    ('979046', 'HARDEN'),    -- Hardenburgh, Ulster
    ('979047', 'HARFOR'),    -- Harford, Cortland
    ('979048', 'HARMON'),    -- Harmony, Chautauqua
    ('979049', 'HARPER'),    -- Harpersfield, Delaware
    ('979050', 'HARRIE'),    -- Harrietstown, Franklin
    ('979051', 'HARRIB'),    -- Harrisburg, Lewis
    ('979052', 'HARRIS'),    -- Harrison, Westchester
    ('979053', 'HARTFO'),    -- Hartford, Washington
    ('979054', 'HARTLA'),    -- Hartland, Niagara
    ('979055', 'HARTSV'),    -- Hartsville, Steuben
    ('979056', 'HARTWI'),    -- Hartwick, Otsego
    ('979057', 'HASTIN'),    -- Hastings, Oswego
    ('979058', 'HAVERS'),    -- Haverstraw, Rockland
    ('979059', 'HEBRON'),    -- Hebron, Washington
    ('979060', 'HECTOR'),    -- Hector, Schuyler
    ('979061', 'HEMPST'),    -- Hempstead, Nassau
    ('979062', 'HENDER'),    -- Henderson, Jefferson
    ('979063', 'HENRIE'),    -- Henrietta, Monroe
    ('979064', 'HERKIM'),    -- Herkimer, Herkimer
    ('979065', 'HERMON'),    -- Hermon, St. Lawrence
    ('979066', 'HIGHLD'),    -- Highland, Sullivan
    ('979067', 'HIGHLA'),    -- Highlands, Orange
    ('979068', 'HILLSD'),    -- Hillsdale, Columbia
    ('979069', 'HINSDA'),    -- Hinsdale, Cattaraugus
    ('979070', 'HOLLAN'),    -- Holland, Erie
    ('979071', 'HOMER'),     -- Homer, Cortland
    ('979072', 'HOOSIC'),    -- Hoosick, Rensselaer
    ('979073', 'HOPE'),      -- Hope, Hamilton
    ('979074', 'HOPEWE'),    -- Hopewell, Ontario
    ('979075', 'HOPKIN'),    -- Hopkinton, St. Lawrence
    ('979076', 'HORICO'),    -- Horicon, Warren
    ('979077', 'HORNBY'),    -- Hornby, Steuben
    ('979078', '-HORNE'),    -- Hornell, Steuben
    ('979079', 'HORNEL'),    -- Hornellsville, Steuben
    ('979080', 'HORSEH'),    -- Horseheads, Chemung
    ('979081', 'HOUNSF'),    -- Hounsfield, Jefferson
    ('979082', 'HOWARD'),    -- Howard, Steuben
    ('979083', '-HUDSO'),    -- Hudson, Columbia
    ('979084', 'HUME'),      -- Hume, Allegany
    ('979085', 'HUMPHR'),    -- Humphrey, Cattaraugus
    ('979086', 'HUNTER'),    -- Hunter, Greene
    ('979087', 'HUNTIN'),    -- Huntington, Suffolk
    ('979088', 'HURLEY'),    -- Hurley, Ulster
    ('979089', 'HURON'),     -- Huron, Wayne
    ('979090', 'HYDE P'),    -- Hyde Park, Dutchess
    ('979091', 'INDEPE'),    -- Independence, Allegany
    ('979092', 'INDIAN'),    -- Indian Lake, Hamilton
    ('979093', 'INLET'),     -- Inlet, Hamilton
    ('979094', 'IRA'),       -- Ira, Cayuga
    ('979095', 'IRONDE'),    -- Irondequoit, Monroe
    ('979096', 'ISCHUA'),    -- Ischua, Cattaraugus
    ('979097', 'ISLIP'),     -- Islip, Suffolk
    ('979098', 'ITALY'),     -- Italy, Yates
    ('979099', '-ITHAC'),    -- Ithaca, Tompkins
    ('979100', 'ITHACA'),    -- Ithaca, Tompkins
    ('979101', 'JACKSO'),    -- Jackson, Washington
    ('979102', '-JAMES'),    -- Jamestown, Chautauqua
    ('979103', 'JASPER'),    -- Jasper, Steuben
    ('979104', 'JAVA'),      -- Java, Wyoming
    ('979105', 'JAY'),       -- Jay, Essex
    ('979106', 'JEFFER'),    -- Jefferson, Schoharie
    ('979107', 'JERUSA'),    -- Jerusalem, Yates
    ('979108', 'JEWETT'),    -- Jewett, Greene
    ('979109', 'JOHNSB'),    -- Johnsburg, Warren
    ('979111', 'JOHNST'),    -- Johnstown, Fulton
    ('979110', '-JOHNS'),    -- Johnstown, Fulton
    ('979112', 'JUNIUS'),    -- Junius, Seneca
    ('979113', 'KEENE'),     -- Keene, Essex
    ('979114', 'KENDAL'),    -- Kendall, Orleans
    ('979740', 'KENT'),      -- Kent, Putnam
    ('979115', 'KIANTO'),    -- Kiantone, Chautauqua
    ('979116', 'KINDER'),    -- Kinderhook, Columbia
    ('979117', 'KINGSB'),    -- Kingsbury, Washington
    ('979118', '-KINGS'),    -- Kingston, Ulster
    ('979119', 'KINGST'),    -- Kingston, Ulster
    ('979120', 'KIRKLA'),    -- Kirkland, Oneida
    ('979121', 'KIRKWO'),    -- Kirkwood, Broome
    ('979122', 'KNOX'),      -- Knox, Albany
    ('979123', 'KORTRI'),    -- Kortright, Delaware
    ('979126', 'LAGRAN'),    -- La Grange, Dutchess
    ('979125', 'LAFAYE'),    -- LaFayette, Onondaga
    ('979124', '-LACKA'),    -- Lackawanna, Erie
    ('979127', 'LAKE G'),    -- Lake George, Warren
    ('979128', 'LAKE L'),    -- Lake Luzerne, Warren
    ('979129', 'LAKE P'),    -- Lake Pleasant, Hamilton
    ('979130', 'LANCAS'),    -- Lancaster, Erie
    ('979131', 'LANSIN'),    -- Lansing, Tompkins
    ('979132', 'LAPEER'),    -- Lapeer, Cortland
    ('979133', 'LAUREN'),    -- Laurens, Otsego
    ('979134', 'LAWREN'),    -- Lawrence, St. Lawrence
    ('979141', 'LERAY'),     -- Le Ray, Jefferson
    ('979142', 'LEROY'),     -- LeRoy, Genesee
    ('979135', 'LEBANO'),    -- Lebanon, Madison
    ('979136', 'LEDYAR'),    -- Ledyard, Cayuga
    ('979137', 'LEE'),       -- Lee, Oneida
    ('979138', 'LEICES'),    -- Leicester, Livingston
    ('979139', 'LENOX'),     -- Lenox, Madison
    ('979140', 'LEON'),      -- Leon, Cattaraugus
    ('979143', 'LEWIS'),     -- Lewis, Essex
    ('979144', 'LEWIS'),     -- Lewis, Lewis
    ('979145', 'LEWISB'),    -- Lewisboro, Westchester
    ('979146', 'LEWIST'),    -- Lewiston, Niagara
    ('979147', 'LEXING'),    -- Lexington, Greene
    ('979148', 'LEYDEN'),    -- Leyden, Lewis
    ('979149', 'LIBERT'),    -- Liberty, Sullivan
    ('979150', 'LIMA'),      -- Lima, Livingston
    ('979151', 'LINCKL'),    -- Lincklaen, Chenango
    ('979152', 'LINCOL'),    -- Lincoln, Madison
    ('979153', 'LINDLE'),    -- Lindley, Steuben
    ('979154', 'LISBON'),    -- Lisbon, St. Lawrence
    ('979155', 'LISLE'),     -- Lisle, Broome
    ('979156', 'LITCHF'),    -- Litchfield, Herkimer
    ('979158', 'LITTLE'),    -- Little Falls, Herkimer
    ('979157', '-LITTL'),    -- Little Falls, Herkimer
    ('979159', 'LITTLV'),    -- Little Valley, Cattaraugus
    ('979160', 'LIVING'),    -- Livingston, Columbia
    ('979161', 'LIVONI'),    -- Livonia, Livingston
    ('979162', 'LLOYD'),     -- Lloyd, Ulster
    ('979163', 'LOCKE'),     -- Locke, Cayuga
    ('979165', 'LOCKPO'),    -- Lockport, Niagara
    ('979164', '-LOCKP'),    -- Lockport, Niagara
    ('979166', 'LODI'),      -- Lodi, Seneca
    ('979167', '-LONG'),     -- Long Beach, Nassau
    ('979168', 'LONG L'),    -- Long Lake, Hamilton
    ('979169', 'LORRAI'),    -- Lorraine, Jefferson
    ('979170', 'LOUISV'),    -- Louisville, St. Lawrence
    ('979171', 'LOWVIL'),    -- Lowville, Lewis
    ('979172', 'LUMBER'),    -- Lumberland, Sullivan
    ('979173', 'LYME'),      -- Lyme, Jefferson
    ('979174', 'LYNDON'),    -- Lyndon, Cattaraugus
    ('979175', 'LYONS'),     -- Lyons, Wayne
    ('979176', 'LYONSD'),    -- Lyonsdale, Lewis
    ('979177', 'LYSAND'),    -- Lysander, Onondaga
    ('979179', 'MACEDO'),    -- Macedon, Wayne
    ('979180', 'MACHIA'),    -- Machias, Cattaraugus
    ('979181', 'MACOMB'),    -- Macomb, St. Lawrence
    ('979182', 'MADISO'),    -- Madison, Madison
    ('979183', 'MADRID'),    -- Madrid, St. Lawrence
    ('979184', 'MAINE'),     -- Maine, Broome
    ('979185', 'MALONE'),    -- Malone, Franklin
    ('979186', 'MALTA'),     -- Malta, Saratoga
    ('979187', 'MAMAKA'),    -- Mamakating, Sullivan
    ('979188', 'MAMARO'),    -- Mamaroneck, Westchester
    ('979189', 'MANCHE'),    -- Manchester, Ontario
    ('979191', 'MANHEI'),    -- Manheim, Herkimer
    ('979192', 'MANLIU'),    -- Manlius, Onondaga
    ('979193', 'MANSFI'),    -- Mansfield, Cattaraugus
    ('979194', 'MARATH'),    -- Marathon, Cortland
    ('979195', 'MARBLE'),    -- Marbletown, Ulster
    ('979196', 'MARCEL'),    -- Marcellus, Onondaga
    ('979197', 'MARCY'),     -- Marcy, Oneida
    ('979198', 'MARILL'),    -- Marilla, Erie
    ('979199', 'MARION'),    -- Marion, Wayne
    ('979200', 'MARLBO'),    -- Marlborough, Ulster
    ('979201', 'MARSHA'),    -- Marshall, Oneida
    ('979202', 'MARTIN'),    -- Martinsburg, Lewis
    ('979203', 'MARYLA'),    -- Maryland, Otsego
    ('979204', 'MASONV'),    -- Masonville, Delaware
    ('979205', 'MASSEN'),    -- Massena, St. Lawrence
    ('979206', 'MAYFIE'),    -- Mayfield, Fulton
    ('979178', 'MCDONO'),    -- McDonough, Chenango
    ('979207', '-MECHA'),    -- Mechanicville, Saratoga
    ('979208', 'MENDON'),    -- Mendon, Monroe
    ('979209', 'MENTZ'),     -- Mentz, Cayuga
    ('979210', 'MEREDI'),    -- Meredith, Delaware
    ('979211', 'MEXICO'),    -- Mexico, Oswego
    ('979212', 'MIDDLB'),    -- Middleburgh, Schoharie
    ('979213', 'MIDDLE'),    -- Middlebury, Wyoming
    ('979214', 'MIDDLF'),    -- Middlefield, Otsego
    ('979215', 'MIDDLS'),    -- Middlesex, Yates
    ('979216', 'MIDDLT'),    -- Middletown, Delaware
    ('979217', '-MIDDL'),    -- Middletown, Orange
    ('979218', 'MILAN'),     -- Milan, Dutchess
    ('979219', 'MILFOR'),    -- Milford, Otsego
    ('979220', 'MILO'),      -- Milo, Yates
    ('979221', 'MILTON'),    -- Milton, Saratoga
    ('979222', 'MINA'),      -- Mina, Chautauqua
    ('979223', 'MINDEN'),    -- Minden, Montgomery
    ('979224', 'MINERV'),    -- Minerva, Essex
    ('979225', 'MINETT'),    -- Minetto, Oswego
    ('979226', 'MINISI'),    -- Minisink, Orange
    ('979227', 'MOHAWK'),    -- Mohawk, Montgomery
    ('979228', 'MOIRA'),     -- Moira, Franklin
    ('979229', 'MONROE'),    -- Monroe, Orange
    ('979230', 'MONTAG'),    -- Montague, Lewis
    ('979231', 'MONTEZ'),    -- Montezuma, Cayuga
    ('979232', 'MONTGO'),    -- Montgomery, Orange
    ('979233', 'MONTOU'),    -- Montour, Schuyler
    ('979234', 'MOOERS'),    -- Mooers, Clinton
    ('979235', 'MORAVI'),    -- Moravia, Cayuga
    ('979236', 'MOREAU'),    -- Moreau, Saratoga
    ('979237', 'MOREHO'),    -- Morehouse, Hamilton
    ('979238', 'MORIAH'),    -- Moriah, Essex
    ('979239', 'MORRIS'),    -- Morris, Otsego
    ('979240', 'MORRIT'),    -- Morristown, St. Lawrence
    ('979241', 'MT HOP'),    -- Mount Hope, Orange
    ('979242', 'MT KIS'),    -- Mount Kisco, Westchester
    ('979243', 'MT MOR'),    -- Mount Morris, Livingston
    ('979244', 'MT PLE'),    -- Mount Pleasant, Westchester
    ('979245', '-MT VE'),    -- Mount Vernon, Westchester
    ('979246', 'MURRAY'),    -- Murray, Orleans
    ('979247', 'NANTIC'),    -- Nanticoke, Broome
    ('979248', 'NAPLES'),    -- Naples, Ontario
    ('979249', 'NAPOLI'),    -- Napoli, Cattaraugus
    ('979250', 'NASSAU'),    -- Nassau, Rensselaer
    ('979251', 'NELSON'),    -- Nelson, Madison
    ('979252', 'NEVERS'),    -- Neversink, Sullivan
    ('979253', 'NEW AL'),    -- New Albion, Cattaraugus
    ('979255', 'NEW BA'),    -- New Baltimore, Greene
    ('979256', 'NEW BE'),    -- New Berlin, Chenango
    ('979257', 'NEW BR'),    -- New Bremen, Lewis
    ('979260', 'NEW CA'),    -- New Castle, Westchester
    ('979264', 'NEW HF'),    -- New Hartford, Oneida
    ('979265', 'NEW HA'),    -- New Haven, Oswego
    ('979266', 'NEW HU'),    -- New Hudson, Allegany
    ('979267', 'NEW LE'),    -- New Lebanon, Columbia
    ('979268', 'NEW LI'),    -- New Lisbon, Otsego
    ('979269', 'NEW PA'),    -- New Paltz, Ulster
    ('979271', '-NEW R'),    -- New Rochelle, Westchester
    ('979272', 'NEW SC'),    -- New Scotland, Albany
    ('979274', 'NEW WI'),    -- New Windsor, Orange
    ('2395220', '-NYC'),     -- New York, New York, Bronx, Kings, Richmond, Queens
    ('979254', 'NEWARK'),    -- Newark Valley, Tioga
    ('979259', 'NEWBUR'),    -- Newburgh, Orange
    ('979258', '-NEWBU'),    -- Newburgh, Orange
    ('979261', 'NEWCOM'),    -- Newcomb, Essex
    ('979262', 'NEWFAN'),    -- Newfane, Niagara
    ('979263', 'NEWFIE'),    -- Newfield, Tompkins
    ('979270', 'NEWPOR'),    -- Newport, Herkimer
    ('979273', 'NEWSTE'),    -- Newstead, Erie
    ('979275', 'NIAGAR'),    -- Niagara, Niagara
    ('979276', '-NIAGA'),    -- Niagara Falls, Niagara
    ('979277', 'NICHOL'),    -- Nichols, Tioga
    ('979278', 'NILES'),     -- Niles, Cayuga
    ('979279', 'NISKAY'),    -- Niskayuna, Schenectady
    ('979280', 'NORFOL'),    -- Norfolk, St. Lawrence
    ('979282', 'N CAST'),    -- North Castle, Westchester
    ('979283', 'N COLL'),    -- North Collins, Erie
    ('979284', 'N DANS'),    -- North Dansville, Livingston
    ('979285', 'N EAST'),    -- North East, Dutchess
    ('979286', 'N ELBA'),    -- North Elba, Essex
    ('979287', 'N GREE'),    -- North Greenbush, Rensselaer
    ('979288', 'N HARM'),    -- North Harmony, Chautauqua
    ('979289', 'N HEMP'),    -- North Hempstead, Nassau
    ('979290', 'N HUDS'),    -- North Hudson, Essex
    ('979291', 'N NORW'),    -- North Norwich, Chenango
    ('979292', 'N SALE'),    -- North Salem, Westchester
    ('979293', '-N TON'),    -- North Tonawanda, Niagara
    ('979281', 'NORTHA'),    -- Northampton, Fulton
    ('979294', 'NORTHU'),    -- Northumberland, Saratoga
    ('979295', 'NORWAY'),    -- Norway, Herkimer
    ('979296', '-NORWI'),    -- Norwich, Chenango
    ('979297', 'NORWIC'),    -- Norwich, Chenango
    ('979298', 'NUNDA'),     -- Nunda, Livingston
    ('979299', 'OAKFIE'),    -- Oakfield, Genesee
    ('979300', 'OGDEN'),     -- Ogden, Monroe
    ('979301', '-OGDEN'),    -- Ogdensburg, St. Lawrence
    ('979302', 'OHIO'),      -- Ohio, Herkimer
    ('979306', 'OLEAN'),     -- Olean, Cattaraugus
    ('979305', '-OLEAN'),    -- Olean, Cattaraugus
    ('979307', 'OLIVE'),     -- Olive, Ulster
    ('979308', '-ONEID'),    -- Oneida, Madison
    ('979310', 'ONEONT'),    -- Oneonta, Otsego
    ('979309', '-ONEON'),    -- Oneonta, Otsego
    ('979311', 'ONONDA'),    -- Onondaga, Onondaga
    ('979313', 'ONTARI'),    -- Ontario, Wayne
    ('979314', 'OPPENH'),    -- Oppenheim, Fulton
    ('979315', 'ORANGE'),    -- Orange, Schuyler
    ('979316', 'ORANGT'),    -- Orangetown, Rockland
    ('979317', 'ORANGV'),    -- Orangeville, Wyoming
    ('979318', 'ORCHAR'),    -- Orchard Park, Erie
    ('979319', 'ORLEAN'),    -- Orleans, Jefferson
    ('979320', 'ORWELL'),    -- Orwell, Oswego
    ('979321', 'OSCEOL'),    -- Osceola, Lewis
    ('979322', 'OSSIAN'),    -- Ossian, Livingston
    ('979323', 'OSSINI'),    -- Ossining, Westchester
    ('979324', 'OSWEGA'),    -- Oswegatchie, St. Lawrence
    ('979326', 'OSWEGO'),    -- Oswego, Oswego
    ('979325', '-OSWEG'),    -- Oswego, Oswego
    ('979327', 'OTEGO'),     -- Otego, Otsego
    ('979328', 'OTISCO'),    -- Otisco, Onondaga
    ('979329', 'OTSEGO'),    -- Otsego, Otsego
    ('979330', 'OTSELI'),    -- Otselic, Chenango
    ('979331', 'OTTO'),      -- Otto, Cattaraugus
    ('979332', 'OVID'),      -- Ovid, Seneca
    ('979333', 'OWASCO'),    -- Owasco, Cayuga
    ('979334', 'OWEGO'),     -- Owego, Tioga
    ('979335', 'OXFORD'),    -- Oxford, Chenango
    ('979336', 'OYSTER'),    -- Oyster Bay, Nassau
    ('979337', 'PALATI'),    -- Palatine, Montgomery
    ('979338', 'PALERM'),    -- Palermo, Oswego
    ('2791540', 'MONROE'),   -- Palm Tree, Orange
    ('979339', 'PALMYR'),    -- Palmyra, Wayne
    ('979340', 'PAMELI'),    -- Pamelia, Jefferson
    ('979341', 'PARIS'),     -- Paris, Oneida
    ('979342', 'PARISH'),    -- Parish, Oswego
    ('979343', 'PARISV'),    -- Parishville, St. Lawrence
    ('979344', 'PARMA'),     -- Parma, Monroe
    ('979345', 'PATTER'),    -- Patterson, Putnam
    ('979346', 'PAVILI'),    -- Pavilion, Genesee
    ('979347', 'PAWLIN'),    -- Pawling, Dutchess
    ('979348', '-PEEKS'),    -- Peekskill, Westchester
    ('979349', 'PELHAM'),    -- Pelham, Westchester
    ('979350', 'PEMBRO'),    -- Pembroke, Genesee
    ('979351', 'PENDLE'),    -- Pendleton, Niagara
    ('979352', 'PENFIE'),    -- Penfield, Monroe
    ('979353', 'PERINT'),    -- Perinton, Monroe
    ('979354', 'PERRY'),     -- Perry, Wyoming
    ('979355', 'PERRYS'),    -- Perrysburg, Cattaraugus
    ('979356', 'PERSIA'),    -- Persia, Cattaraugus
    ('979357', 'PERTH'),     -- Perth, Fulton
    ('979358', 'PERU'),      -- Peru, Clinton
    ('979359', 'PETERS'),    -- Petersburgh, Rensselaer
    ('979360', 'PHARSA'),    -- Pharsalia, Chenango
    ('979361', 'PHELPS'),    -- Phelps, Ontario
    ('979362', 'PHILAD'),    -- Philadelphia, Jefferson
    ('979363', 'PHILIP'),    -- Philipstown, Putnam
    ('979364', 'PIERCE'),    -- Piercefield, St. Lawrence
    ('979365', 'PIERRE'),    -- Pierrepont, St. Lawrence
    ('979366', 'PIKE'),      -- Pike, Wyoming
    ('979367', 'PINCKN'),    -- Pinckney, Lewis
    ('979368', 'PINE P'),    -- Pine Plains, Dutchess
    ('979369', 'PITCAI'),    -- Pitcairn, St. Lawrence
    ('979370', 'PITCHE'),    -- Pitcher, Chenango
    ('979371', 'PITTSD'),    -- Pittsfield, Otsego
    ('979372', 'PITTSF'),    -- Pittsford, Monroe
    ('979373', 'PITTST'),    -- Pittstown, Rensselaer
    ('979374', 'PLAINF'),    -- Plainfield, Otsego
    ('979375', 'PLATTE'),    -- Plattekill, Ulster
    ('979376', '-PLATT'),    -- Plattsburgh, Clinton
    ('979377', 'PLATTS'),    -- Plattsburgh, Clinton
    ('979378', 'PLEASA'),    -- Pleasant Valley, Dutchess
    ('979379', 'PLYMOU'),    -- Plymouth, Chenango
    ('979380', 'POESTE'),    -- Poestenkill, Rensselaer
    ('979381', 'POLAND'),    -- Poland, Chautauqua
    ('979382', 'POMFRE'),    -- Pomfret, Chautauqua
    ('979383', 'POMPEY'),    -- Pompey, Onondaga
    ('979387', '-PORT'),     -- Port Jervis, Orange
    ('979385', 'PORTAG'),    -- Portage, Livingston
    ('979386', 'PORTER'),    -- Porter, Niagara
    ('979388', 'PORTLA'),    -- Portland, Chautauqua
    ('979389', 'PORTVI'),    -- Portville, Cattaraugus
    ('979390', 'POTSDA'),    -- Potsdam, St. Lawrence
    ('979391', 'POTTER'),    -- Potter, Yates
    ('979393', 'POUGHK'),    -- Poughkeepsie, Dutchess
    ('979392', '-POUGH'),    -- Poughkeepsie, Dutchess
    ('979394', 'POUND'),     -- Pound Ridge, Westchester
    ('979395', 'PRATTS'),    -- Prattsburgh, Steuben
    ('979396', 'PRATTV'),    -- Prattsville, Greene
    ('979397', 'PREBLE'),    -- Preble, Cortland
    ('979398', 'PRESTO'),    -- Preston, Chenango
    ('979399', 'PRINCE'),    -- Princetown, Schenectady
    ('979400', 'PROVID'),    -- Providence, Saratoga
    ('979401', 'PULTEN'),    -- Pulteney, Steuben
    ('979402', 'PUTNAM'),    -- Putnam, Washington
    ('979403', 'PUTNAV'),    -- Putnam Valley, Putnam
    ('979405', 'QUEENS'),    -- Queensbury, Warren
    ('979406', 'RAMAPO'),    -- Ramapo, Rockland
    ('979407', 'RANDOL'),    -- Randolph, Cattaraugus
    ('979408', 'RATHBO'),    -- Rathbone, Steuben
    ('979409', 'READIN'),    -- Reading, Schuyler
    ('979411', 'RED HK'),    -- Red Hook, Dutchess
    ('979412', 'RED HO'),    -- Red House, Cattaraugus
    ('979410', 'REDFIE'),    -- Redfield, Oswego
    ('979413', 'REMSEN'),    -- Remsen, Oneida
    ('979414', '-RENSS'),    -- Rensselaer, Rensselaer
    ('979415', 'RENSSE'),    -- Rensselaerville, Albany
    ('979416', 'RHINEB'),    -- Rhinebeck, Dutchess
    ('979417', 'RICHFI'),    -- Richfield, Otsego
    ('979418', 'RICHFO'),    -- Richford, Tioga
    ('979419', 'RICHLA'),    -- Richland, Oswego
    ('978648', 'RICHMO'),    -- Richmond, Ontario
    ('979421', 'RICHMV'),    -- Richmondville, Schoharie
    ('979422', 'RIDGEW'),    -- Ridgeway, Orleans
    ('979423', 'RIGA'),      -- Riga, Monroe
    ('979424', 'RIPLEY'),    -- Ripley, Chautauqua
    ('979425', 'RIVERH'),    -- Riverhead, Suffolk
    ('979426', '-ROCHE'),    -- Rochester, Monroe
    ('979427', 'ROCHES'),    -- Rochester, Ulster
    ('979428', 'ROCKLA'),    -- Rockland, Sullivan
    ('979429', 'RODMAN'),    -- Rodman, Jefferson
    ('979430', '-ROME'),     -- Rome, Oneida
    ('979431', 'ROMULU'),    -- Romulus, Seneca
    ('979432', 'ROOT'),      -- Root, Montgomery
    ('979433', 'ROSE'),      -- Rose, Wayne
    ('979434', 'ROSEBO'),    -- Roseboom, Otsego
    ('979435', 'ROSEND'),    -- Rosendale, Ulster
    ('979436', 'ROSSIE'),    -- Rossie, St. Lawrence
    ('979437', 'ROTTER'),    -- Rotterdam, Schenectady
    ('979438', 'ROXBUR'),    -- Roxbury, Delaware
    ('979439', 'ROYALT'),    -- Royalton, Niagara
    ('979440', 'RUSH'),      -- Rush, Monroe
    ('979441', 'RUSHFO'),    -- Rushford, Allegany
    ('979442', 'RUSSEL'),    -- Russell, St. Lawrence
    ('979443', 'RUSSIA'),    -- Russia, Herkimer
    ('979444', 'RUTLAN'),    -- Rutland, Jefferson
    ('979445', '-RYE'),      -- Rye, Westchester
    ('979446', 'RYE'),       -- Rye, Westchester
    ('979450', '-SALAM'),    -- Salamanca, Cattaraugus
    ('979451', 'SALAMA'),    -- Salamanca, Cattaraugus
    ('979452', 'SALEM'),     -- Salem, Washington
    ('979453', 'SALINA'),    -- Salina, Onondaga
    ('979454', 'SALISB'),    -- Salisbury, Herkimer
    ('979455', 'SAND L'),    -- Sand Lake, Rensselaer
    ('979456', 'SANDY'),     -- Sandy Creek, Oswego
    ('979457', 'SANFOR'),    -- Sanford, Broome
    ('979458', 'SANGER'),    -- Sangerfield, Oneida
    ('979459', 'SANTA'),     -- Santa Clara, Franklin
    ('979460', 'SARANA'),    -- Saranac, Clinton
    ('979461', 'SARATO'),    -- Saratoga, Saratoga
    ('979462', '-SARAT'),    -- Saratoga Springs, Saratoga
    ('979463', 'SARDIN'),    -- Sardinia, Erie
    ('979464', 'SAUGER'),    -- Saugerties, Ulster
    ('979465', 'SAVANN'),    -- Savannah, Wayne
    ('979466', 'SCARSD'),    -- Scarsdale, Westchester
    ('979467', 'SCHAGH'),    -- Schaghticoke, Rensselaer
    ('979468', '-SCHEN'),    -- Schenectady, Schenectady
    ('979469', 'SCHODA'),    -- Schodack, Rensselaer
    ('979470', 'SCHOHA'),    -- Schoharie, Schoharie
    ('979471', 'SCHROE'),    -- Schroeppel, Oswego
    ('979472', 'SCHROO'),    -- Schroon, Essex
    ('979473', 'SCHUYL'),    -- Schuyler, Herkimer
    ('979474', 'SCHUYF'),    -- Schuyler Falls, Clinton
    ('979475', 'SCIO'),      -- Scio, Allegany
    ('979476', 'SCIPIO'),    -- Scipio, Cayuga
    ('979477', 'SCOTT'),     -- Scott, Cortland
    ('979478', 'SCRIBA'),    -- Scriba, Oswego
    ('979479', 'SEMPRO'),    -- Sempronius, Cayuga
    ('979480', 'SENECA'),    -- Seneca, Ontario
    ('979481', 'SENECF'),    -- Seneca Falls, Seneca
    ('979482', 'SENNET'),    -- Sennett, Cayuga
    ('979483', 'SEWARD'),    -- Seward, Schoharie
    ('979484', 'SHANDA'),    -- Shandaken, Ulster
    ('979485', 'SHARON'),    -- Sharon, Schoharie
    ('979486', 'SHAWAN'),    -- Shawangunk, Ulster
    ('979487', 'SHELBY'),    -- Shelby, Orleans
    ('979488', 'SHELDO'),    -- Sheldon, Wyoming
    ('979489', 'SHELTE'),    -- Shelter Island, Suffolk
    ('979490', 'SHERBU'),    -- Sherburne, Chenango
    ('979491', 'SHERID'),    -- Sheridan, Chautauqua
    ('979492', 'SHERMA'),    -- Sherman, Chautauqua
    ('979493', '-SHERR'),    -- Sherrill, Oneida
    ('979495', 'SIDNEY'),    -- Sidney, Delaware
    ('979496', 'SKANEA'),    -- Skaneateles, Onondaga
    ('979497', 'SMITHF'),    -- Smithfield, Madison
    ('979498', 'SMITHT'),    -- Smithtown, Suffolk
    ('979499', 'SMITHV'),    -- Smithville, Chenango
    ('979500', 'SMYRNA'),    -- Smyrna, Chenango
    ('979501', 'SODUS'),     -- Sodus, Wayne
    ('979502', 'SOLON'),     -- Solon, Cortland
    ('979503', 'SOMERS'),    -- Somers, Westchester
    ('979504', 'SOMERT'),    -- Somerset, Niagara
    ('979506', 'S BRIS'),    -- South Bristol, Ontario
    ('979510', 'S VALL'),    -- South Valley, Cattaraugus
    ('979505', 'SOUTHA'),    -- Southampton, Suffolk
    ('979507', 'SOUTHE'),    -- Southeast, Putnam
    ('979508', 'SOUTHO'),    -- Southold, Suffolk
    ('979509', 'SOUTHP'),    -- Southport, Chemung
    ('979511', 'SPAFFO'),    -- Spafford, Onondaga
    ('979512', 'SPARTA'),    -- Sparta, Livingston
    ('979513', 'SPENCE'),    -- Spencer, Tioga
    ('979514', 'SPRINF'),    -- Springfield, Otsego
    ('979515', 'SPRINP'),    -- Springport, Cayuga
    ('979516', 'SPRING'),    -- Springwater, Livingston
    ('979447', 'ST ARM'),    -- St. Armand, Essex
    ('979448', 'ST JOH'),    -- St. Johnsville, Montgomery
    ('979517', 'STAFFO'),    -- Stafford, Genesee
    ('979518', 'STAMFO'),    -- Stamford, Delaware
    ('979519', 'STANFO'),    -- Stanford, Dutchess
    ('979520', 'STARK'),     -- Stark, Herkimer
    ('979521', 'STARKE'),    -- Starkey, Yates
    ('979523', 'STEPHE'),    -- Stephentown, Rensselaer
    ('979524', 'STERLI'),    -- Sterling, Cayuga
    ('979525', 'STEUBE'),    -- Steuben, Oneida
    ('979526', 'STILLW'),    -- Stillwater, Saratoga
    ('979527', 'STOCKB'),    -- Stockbridge, Madison
    ('979528', 'STOCKH'),    -- Stockholm, St. Lawrence
    ('979529', 'STOCKP'),    -- Stockport, Columbia
    ('979530', 'STOCKT'),    -- Stockton, Chautauqua
    ('979531', 'STONY'),     -- Stony Creek, Warren
    ('979532', 'STONYP'),    -- Stony Point, Rockland
    ('979533', 'STRATF'),    -- Stratford, Fulton
    ('979534', 'STUYVE'),    -- Stuyvesant, Columbia
    ('979535', 'SULLIV'),    -- Sullivan, Madison
    ('979536', 'SUMMER'),    -- Summerhill, Cayuga
    ('979537', 'SUMMIT'),    -- Summit, Schoharie
    ('979538', 'SWEDEN'),    -- Sweden, Monroe
    ('979539', '-SYRAC'),    -- Syracuse, Onondaga
    ('979540', 'TAGHKA'),    -- Taghkanic, Columbia
    ('979541', 'TAYLOR'),    -- Taylor, Cortland
    ('979542', 'THERES'),    -- Theresa, Jefferson
    ('979543', 'THOMPS'),    -- Thompson, Sullivan
    ('979544', 'THROOP'),    -- Throop, Cayuga
    ('979545', 'THURMA'),    -- Thurman, Warren
    ('979546', 'THURST'),    -- Thurston, Steuben
    ('979547', 'TICOND'),    -- Ticonderoga, Essex
    ('979548', 'TIOGA'),     -- Tioga, Tioga
    ('979549', 'TOMPKI'),    -- Tompkins, Delaware
    ('979551', 'TONAWA'),    -- Tonawanda, Erie
    ('979550', '-TONAW'),    -- Tonawanda, Erie
    ('979555', 'TORREY'),    -- Torrey, Yates
    ('979556', 'TRENTO'),    -- Trenton, Oneida
    ('979557', 'TRIANG'),    -- Triangle, Broome
    ('979558', 'TROUPS'),    -- Troupsburg, Steuben
    ('979559', '-TROY'),     -- Troy, Rensselaer
    ('979560', 'TRUXTO'),    -- Truxton, Cortland
    ('979561', 'TULLY'),     -- Tully, Onondaga
    ('978671', 'TUPPER'),    -- Tupper Lake, Franklin
    ('979562', 'TURIN'),     -- Turin, Lewis
    ('979563', 'TUSCAR'),    -- Tuscarora, Steuben
    ('979565', 'TUSTEN'),    -- Tusten, Sullivan
    ('979566', 'TUXEDO'),    -- Tuxedo, Orange
    ('979567', 'TYRE'),      -- Tyre, Seneca
    ('979568', 'TYRONE'),    -- Tyrone, Schuyler
    ('979569', 'ULSTER'),    -- Ulster, Ulster
    ('979570', 'ULYSSE'),    -- Ulysses, Tompkins
    ('979571', 'UNADIL'),    -- Unadilla, Otsego
    ('979572', 'UNION'),     -- Union, Broome
    ('979573', 'UNIONV'),    -- Union Vale, Dutchess
    ('979574', 'URBANA'),    -- Urbana, Steuben
    ('979575', '-UTICA'),    -- Utica, Oneida
    ('979576', 'VAN BU'),    -- Van Buren, Onondaga
    ('979577', 'VAN ET'),    -- Van Etten, Chemung
    ('979578', 'VARICK'),    -- Varick, Seneca
    ('979579', 'VENICE'),    -- Venice, Cayuga
    ('979580', 'VERNON'),    -- Vernon, Oneida
    ('979581', 'VERONA'),    -- Verona, Oneida
    ('979582', 'VESTAL'),    -- Vestal, Broome
    ('979583', 'VETERA'),    -- Veteran, Chemung
    ('979584', 'VICTOR'),    -- Victor, Ontario
    ('979585', 'VICTOY'),    -- Victory, Cayuga
    ('979586', 'VIENNA'),    -- Vienna, Oneida
    ('979587', 'VILLEN'),    -- Villenova, Chautauqua
    ('979588', 'VIRGIL'),    -- Virgil, Cortland
    ('979589', 'VOLNEY'),    -- Volney, Oswego
    ('979590', 'WADDIN'),    -- Waddington, St. Lawrence
    ('979591', 'WALES'),     -- Wales, Erie
    ('979592', 'WALLKI'),    -- Wallkill, Orange
    ('979593', 'WALTON'),    -- Walton, Delaware
    ('979594', 'WALWOR'),    -- Walworth, Wayne
    ('979595', 'WAPPIN'),    -- Wappinger, Dutchess
    ('979596', 'W ALMO'),    -- Ward, Allegany
    ('979597', 'WARREN'),    -- Warren, Herkimer
    ('979598', 'WARREB'),    -- Warrensburg, Warren
    ('979599', 'WARSAW'),    -- Warsaw, Wyoming
    ('979600', 'WARWIC'),    -- Warwick, Orange
    ('979601', 'WASHIN'),    -- Washington, Dutchess
    ('979602', 'WATERF'),    -- Waterford, Saratoga
    ('979603', 'WATERL'),    -- Waterloo, Seneca
    ('979604', '-WATER'),    -- Watertown, Jefferson
    ('979605', 'WATERT'),    -- Watertown, Jefferson
    ('979606', '-WATEV'),    -- Watervliet, Albany
    ('979607', 'WATSON'),    -- Watson, Lewis
    ('979608', 'WAVERL'),    -- Waverly, Franklin
    ('979609', 'WAWARS'),    -- Wawarsing, Ulster
    ('979610', 'WAWAYA'),    -- Wawayanda, Orange
    ('979611', 'WAYLAN'),    -- Wayland, Steuben
    ('979612', 'WAYNE'),     -- Wayne, Steuben
    ('979613', 'WEBB'),      -- Webb, Herkimer
    ('979614', 'WEBSTE'),    -- Webster, Monroe
    ('979615', 'WELLS'),     -- Wells, Hamilton
    ('979616', 'WARD'),      -- Wellsville, Allegany
    ('979617', 'WELLSV'),    -- West Almond, Allegany
    ('979618', 'W BLOO'),    -- West Bloomfield, Ontario
    ('979623', 'W MONR'),    -- West Monroe, Oswego
    ('979626', 'W SENE'),    -- West Seneca, Erie
    ('979627', 'W SPAR'),    -- West Sparta, Livingston
    ('979628', 'W TURI'),    -- West Turin, Lewis
    ('979629', 'W UNIO'),    -- West Union, Steuben
    ('979619', 'WESTER'),    -- Westerlo, Albany
    ('979620', 'WESTEN'),    -- Western, Oneida
    ('979621', 'WESTFI'),    -- Westfield, Chautauqua
    ('979622', 'WESTFO'),    -- Westford, Otsego
    ('979624', 'WESTMO'),    -- Westmoreland, Oneida
    ('979625', 'WESTPO'),    -- Westport, Essex
    ('979630', 'WESTVI'),    -- Westville, Franklin
    ('979631', 'WETHER'),    -- Wethersfield, Wyoming
    ('979632', 'WHEATF'),    -- Wheatfield, Niagara
    ('979633', 'WHEATL'),    -- Wheatland, Monroe
    ('979634', 'WHEELE'),    -- Wheeler, Steuben
    ('979635', 'WHITE'),     -- White Creek, Washington
    ('979637', '-WHITE'),    -- White Plains, Westchester
    ('979636', 'WHITEH'),    -- Whitehall, Washington
    ('979638', 'WHITES'),    -- Whitestown, Oneida
    ('979639', 'WILLET'),    -- Willet, Cortland
    ('979640', 'WILLIA'),    -- Williamson, Wayne
    ('979641', 'WILLIM'),    -- Williamstown, Oswego
    ('979642', 'WILLIN'),    -- Willing, Allegany
    ('979643', 'WILLSB'),    -- Willsboro, Essex
    ('979644', 'WILMIN'),    -- Wilmington, Essex
    ('979645', 'WILNA'),     -- Wilna, Jefferson
    ('979646', 'WILSON'),    -- Wilson, Niagara
    ('979647', 'WILTON'),    -- Wilton, Saratoga
    ('979648', 'WINDHA'),    -- Windham, Greene
    ('979649', 'WINDSO'),    -- Windsor, Broome
    ('979650', 'WINFIE'),    -- Winfield, Herkimer
    ('979651', 'WIRT'),      -- Wirt, Allegany
    ('979652', 'WOLCOT'),    -- Wolcott, Wayne
    ('979653', 'WOODBU'),    -- Woodbury, Orange
    ('979654', 'WOODHU'),    -- Woodhull, Steuben
    ('979655', 'WOODST'),    -- Woodstock, Ulster
    ('979656', 'WORCES'),    -- Worcester, Otsego
    ('979657', 'WORTH'),     -- Worth, Jefferson
    ('979658', 'WRIGHT'),    -- Wright, Schoharie
    ('979659', 'YATES'),     -- Yates, Orleans
    ('979660', '-YONKE'),    -- Yonkers, Westchester
    ('979661', 'YORK'),      -- York, Livingston
    ('979662', 'YORKSH'),    -- Yorkshire, Cattaraugus
    ('979663', 'YORKTO')     -- Yorktown, Westchester
)
UPDATE districts.town_city tc
SET abbrev = codes.abbrev
FROM codes
WHERE tc.gnis_id = codes.gnis_id;

--Corrects to use legal names.
UPDATE districts.town_city
SET name = replace(name, 'St ', 'St. '),
    county = replace(county, 'St ', 'St. ')
WHERE name LIKE '%St %' OR county LIKE '%St %';

-- Municipalities the mapping above doesn't cover. Add a code for each before re-running.
\echo 'Municipalities with no abbrev (add them to this file):'
SELECT tc.gnis_id, tc.name
FROM districts.town_city tc
WHERE tc.abbrev IS NULL
ORDER BY tc.name;
