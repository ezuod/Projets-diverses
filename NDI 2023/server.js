const http = require('http');
const fs = require('fs');
const path = require('path');

const server = http.createServer((req, res) => {
    if (req.url === '/') {
        const filePath = path.join(__dirname, 'public', 'index.html');
        readFile(filePath, 'text/html', res);
    } else {
        const pageName = req.url.replace('/', '').replace('.html', '');
        const filePath = path.join(__dirname, 'public', req.url);
        if (fs.existsSync(filePath)) {
            readFile(filePath, getContentType(req.url), res);
        } else {
            if (isValidPage(pageName)) {
                serveDynamicPage(res, pageName);
            } else {
                res.writeHead(404, { 'Content-Type': 'text/plain' });
                res.end('Not Found');
            }
        }
    }
});

const port = 8181;
server.listen(port, () => {
    console.log(`Server running at http://localhost:${port}/`);
});

function readFile(filePath, contentType, res) {
    fs.access(filePath, fs.constants.F_OK, (err) => {
        if (err) {
            res.writeHead(404, { 'Content-Type': 'text/plain' });
            res.end('Not Found');
        } else {
            const fileContent = fs.readFileSync(filePath);
            res.writeHead(200, { 'Content-Type': contentType });
            res.end(fileContent);
        }
    });
}

const templatePath = path.join(__dirname, 'public', 'template.html');

function replacePlaceholders(content, replacements) {
    for (const [placeholder, value] of Object.entries(replacements)) {
        const regex = new RegExp(`{{${placeholder}}}`, 'g');
        content = content.replace(regex, value);
    }
    return content;
}

function servePage(res, pageTitle, pageContent) {
    fs.readFile(templatePath, 'utf8', (err, templateContent) => {
        if (err) {
            res.writeHead(500, { 'Content-Type': 'text/plain' });
            res.end('Internal Server Error');
        } else {
            const replacedContent = replacePlaceholders(templateContent, {
                pageTitle: pageTitle,
                pageContent: pageContent,
            });
            res.writeHead(200, { 'Content-Type': 'text/html' });
            res.end(replacedContent);
        }
    });
}

function serveDynamicPage(res, pageName) {
    const dynamicContent = {
        'energie': `
            <p><B><strong>L'énergie est essentielle à notre quotidien</strong></B> et elle est produite à partir de différentes sources. Cependant ces sources ont une répercussion sur les émissions de gaz à effet de serre et par conséquent sur le changement climatique. </p>
            <p>Les énergies fossiles, traditionnellement les plus utilisées, sont des énergies non renouvelables comprenant le charbon, le pétrole et le gaz naturel fournissant environ 80 % de l'énergie mondiale. Cependant, <strong>elles contribuent de manière alarmante à l’augmentation de gaz à effet de serre dans l’atmosphère.</strong> Les données scientifiques sont claires : afin d’éviter les pires effets des changements climatiques, il faut réduire les émissions pratiquement de moitié d’ici 2030.</p>
            <p>Face à cette réalité, les énergies renouvelables telles que le solaire, l'éolien, et l'hydroélectricité, émergent comme des alternatives cruciales pour réduire les émissions de GES. </p>
            <p><strong>Les sources d’énergie renouvelables jouent un rôle essentiel dans la transition vers un système énergétique plus durable</strong> étant donné qu’elles sont disponibles en abondance partout autour de nous grâce au soleil, au vent, à l’eau, aux déchets et à la chaleur de la Terre. Elles sont durables à long terme et ne rejettent que peu ou pas de gaz à effet de serre ou de polluants dans l’air. Elles contribuent ainsi aux efforts mondiaux pour atténuer le changement climatique.</p>
            <p>L'énergie renouvelable constitue aujourd’hui <strong>la solution énergétique la moins chère</strong> dans la plupart des pays du monde. Cela permet à tous les pays, notamment aux pays à revenu faible ou intermédiaire, d’en profiter.
            Par ailleurs, les énergies renouvelables favorisent la création d’emploi dans des secteurs tels que l’installation, la maintenance et la production d'équipement. Ainsi, selon l’AIE (Agence Internationale de l'Énergie), d'ici à 2030, environ cinq millions d’emplois pourraient être supprimés dans le secteur de la production de combustibles fossiles, selon les estimations, 14 millions de nouveaux emplois pourraient être créés dans le secteur des énergies propres, entraînant un gain net de 9 millions d’emplois.
            </p>
            <p>Le nucléaire est également souvent considéré comme une option à faible émission de carbone pouvant jouer un rôle dans la lutte contre le changement climatique car il ne produit pas de dioxyde de carbone lors de son exploitation, et peut fournir une production d'électricité stable. Cependant les progrès du nucléaire ont été plus lents que prévus et ne semblent pas sans risque pour atteindre, des Objectifs de Développement Durables, due aux déchets nucléaires, à la sécurité et aux coûts élevés liés aux centrales nucléaires.
            </p>
        `,
        'transport': `
            <p>Les transports sont une source significative d'émissions de gaz à effet de serre (GES), contribuant de manière importante au changement climatique. Les principaux gaz émis par les transports sont le dioxyde de carbone (CO2), le méthane (CH4), et le protoxyde d'azote (N2O). En France, il représente 31 % des émissions.</p>
            <p>Les solutions pour réduire les gaz à effet de serre des transports:</p>
            <p><B>A l’échelle individuelle :</B></p>
            <ul>
                <li>Utilisation des transports en commun.</li>
                <li>Le covoiturage</li>
                <li>Le télétravail</li>
                <li>Mobilité douce (utilisation de vélo, marche à pieds,...)</li>
                <li>Favoriser l’utilisation des carburants alternatifs (hydrogène, gaz naturel,...)</li>
            </ul>
            <p>Ces solutions sont nécessaires, mais ne suffisent pas. Elles doivent être soutenues par des mesures politiques qui favorisent des solutions.</p>
            <p><B>A l’échelle Politiques :</B></p>
            <ul>
                <li>Créer des normes d'émissions pour les véhicules.</li>
                <li>Développement des transports en commun.</li>
                <li>Promotion des véhicules électriques</li>
                <li>Investissement dans les infrastructures de recharge</li>
            </ul>
            <p>Ces solutions nécessitent souvent une approche intégrée, la coopération entre différents niveaux de gouvernement, ainsi qu'une consultation étroite avec l'industrie et la société civile.</p>
            <p>L'utilisation des <B>véhicules électriques</B> est également une solution efficace pour diminuer les émissions de gaz à effet de serre. D’autant plus que la voiture électrique va devenir incontournable car l’UE et les 27 États membres ont voté l’interdiction à la vente des voitures neuves thermique (essence et diesel) en 2035. Il faut savoir qu’un véhicule électrique n’est pas “propre”. En revanche, il peut être qualifié de “plus propre que les véhicules thermique”. La pollution carbone de la voiture à batterie ne vient pas de son utilisation… Mais plutôt sa production. Mais ces émissions sont très largement compensées lors de l’utilisation du véhicule, le véhicule électrique n’émettant ni CO2, ni particules lorsqu’il circule. Lors de sa durée de vie complète – de la fabrication jusqu’à la fin de vie -, une voiture électrique émettra en moyenne 3 fois moins de CO2e (en gCO2e/km) que son équivalent thermique. Le gain est donc bien là. Et il n’est pas négligeable, en particulier en France.</p>
            <p>Les <B>transports aériens</B> représentent 5.3% des émissions de gaz à effets de serre dans les secteurs du transport en France. D’après le GIEC (Groupe d'experts intergouvernemental sur l'évolution du climat) aucune amélioration au niveau énergétique ne permettra de suivre le rythme de la croissance prévue du transport aérien. La limitation du trafic resterait donc le moyen le plus efficace de réduire les émissions du secteur.</p>
        `,
        'alimentation': `
            <p>L'agriculture, pilier essentiel de notre alimentation quotidienne, joue un rôle majeur en fournissant les ressources alimentaires nécessaires à notre population croissante. Cependant, cette activité humaine intensive a des conséquences significatives sur l'environnement en contribuant aux émissions de gaz à effet de serre. Les effets de serre causés par l'agriculture sont un enjeu crucial dans le débat sur le changement climatique. Ils sont à l’origine du réchauffement planétaire et ont des implications profondes sur la durabilité de notre planète.</p>
            <p><strong>La production agricole représente 20% des émissions territoriales françaises.</strong></p>
            <p>Cela est dû aux gaz à effet de serre générés par l’élevage, l’épandage d’engrais azotés, les serres ainsi que les engins agricoles. émises sur le sol français. Tandis que les émissions de gaz à effet de serre liées à l’alimentation des ménages français représentent 24% de leur empreinte carbone.</p>
            <p><strong>Comment y remédier ?</strong></p>
            <p>La production de viande étant la plus polluante, l’élevage représente 40% de l’agriculture, une réduction de sa consommation aurait un impact radical sur la production de CO2. Une personne végétarienne produit 1,5 fois moins de gaz à effet de serre alors qu’elles ne représentent que 5% de la population mondiale. De plus, cela nous permettrait de manger plus équilibré, augmentant même la durée de vie moyenne.</p>
            <p><strong>Le steak végétal ne serait pas vraiment le futur.</strong></p>
            <p>Il est très facile de trouver sur le web des débats sur le faible apport d’un régime basé exclusivement sur des végétaux. Effectivement, plusieurs études montrent que les végétariens sont plus exposés aux maladies et ont tendance à mourir plus jeune. Et selon les dires du Professeur Ulrich Voderholzer : “ [les promesses faites par les associations végétariennes] ne sont pas scientifiquement prouvées. ”</p>
            <p>De plus, les végétaux les plus nourrissants, comme le soja, sont souvent difficiles à cultiver. Il est donc importé, ce qui est la cause principale de la production de CO2.</p>
            <p>Finalement, la production agricole pourrait être diminuée mais pas complètement supprimée pour le bien de la santé de la population mondiale. Il est aussi préférable de prioriser les aliments produits localement.</p>
        `,
        'batiment': `
            <p>Les bâtiments sont responsables de 20 à 25% des émissions de gaz à effet de serre et nécessitent une rénovation énergétique urgente. Il consomme 47% de l’énergie finale, ce qui fait de lui le plus gros consommateur d’énergie de tous les secteurs économiques, juste devant le secteur des transports (44%). La rénovation énergétique joue donc un rôle central</p>
            <p>dans la lutte contre le dérèglement climatique. Cette action réduirait non seulement la consommation d'énergie, mais améliorerait également le confort et la santé des occupants tout en luttant contre la précarité énergétique touchant 20% des ménages en France.</p>
            <p>Des obstacles à l'efficacité énergétique continuent de persister et cela malgré la disponibilité des technologies. Identifier et surmonter ces barrières sont essentiels pour une rénovation réussie.</p>
            <p><B>Des habitudes à changer</B></p>
            <p>Une solution pour une rénovation efficace serait d’augmenter les budgets des programmes de rénovation afin d’atteindre les objectifs annuels. La rénovation énergétique présente également des avantages économiques, créant des emplois et maîtrisant les coûts énergétiques. Il serait aussi préférable de réduire le plus possible nos consommations d’électricité tel que le chauffage ou l’utilisation des lumières.</p>
            <p><B>Améliorer les réglementations</B></p>
            <p>Une révision du cadre réglementaire vers des normes de performance énergétique plus élevées est nécessaire. De plus, les aides financières doivent être simplifiées et accessibles à tous, en particulier aux ménages modestes. Il est impératif d'engager un plan massif de rénovation énergétique en surmontant les obstacles, en allouant des ressources adéquates, et en révisant les politiques pour créer des bâtiments plus durables et économes en énergie.</p>
        `,
        'industrie': `
            <p>Selon le Ministère de la Transition écologique, l'industrie représente environ 10% des émissions de gaz à effet de serre en France. Pour contribuer à la <B>neutralité carbone</B>, le « net zero émission », les entreprises doivent s’engager dans la voie de la décarbonation.</p>
            <p>La <B>décarbonisation</B> du secteur permettra ainsi aux déploiement des sites industriels innovants et compétitifs. Pour réduire la part d’émissions de gaz à effet de serre des industries, les efforts coordonnés entre les gouvernements, les entreprises, et la société civile sont alors essentiels pour atteindre des résultats significatifs.</p>
            <p>Afin de réduire la dépendance des industries vis-à-vis des combustibles fossiles, ils peuvent utiliser <B>les énergies renouvelables</B> pour alimenter les opérations industrielles. Ils peuvent également favoriser l’utilisation de <B>matériaux recyclés</B> pour diminuer l’utilisation des matières premières. Le développement de l’économie circulaire permettrait ainsi d'améliorer la qualité du tri et de la collecte afin de permettre le déploiement de solutions de recyclage pour la production de matériaux secondaires.</p>
            <p><B>La mise en place de normes et réglementation</B> est donc nécessaire afin d’encadrer ces activités et pour encourager l'adoption de pratiques plus durables et la réduction des émissions.</p>`
        ,
    };
    servePage(res, pageName.toUpperCase(), dynamicContent[pageName]);
}

function isValidPage(pageName) {
    const validPages = ['energie', 'transport', 'alimentation', 'batiment', 'industrie'];
    return validPages.includes(pageName);
}

function getContentType(url) {
    const extname = path.extname(url);
    switch (extname) {
        case '.html':
            return 'text/html';
        case '.css':
            return 'text/css';
        case '.js':
            return 'text/javascript';
        case '.png':
            return 'image/png';
        case '.jpg':
            return 'image/jpg';
        default:
            return 'application/octet-stream';
    }
}
