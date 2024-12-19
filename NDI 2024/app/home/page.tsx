import React from 'react';
import './acceuil.css'; // Pour les styles personnalisés

const OceanPage: React.FC = () => {
  return (
    <div className="ocean-container">
    
      <header className="ocean-header">
        <h1>Bienvenue dans le Monde de l'Océan</h1>
        <p>Explorez la beauté et la diversité des océans de notre planète.</p>
      </header>

      <main className="ocean-main">
        <section className="ocean-introduction">
          <h2>Les Océans: Un Univers Mystérieux</h2>
          <p>
            Les océans couvrent plus de 70% de la surface de la Terre, abritant des écosystèmes incroyablement
            diversifiés et des créatures fascinantes. Du bleu profond des mers aux récifs coralliens colorés,
            l'océan est à la fois source de vie et d'inspiration.
          </p>
        </section>

        <section className="ocean-impact">
          <h2>L'Impact de l'Homme sur l'Océan</h2>
          <p>
            L'activité humaine a un impact significatif sur nos océans. La pollution plastique, le changement
            climatique et la surpêche sont des menaces majeures qui affectent la vie marine. Il est important
            de prendre soin de nos océans pour préserver leur biodiversité.
          </p>
        </section>

        <section className="ocean-exploration">
          <h2>Explorer les Profondeurs</h2>
          <p>
            Grâce aux avancées technologiques, les explorateurs et scientifiques peuvent désormais plonger dans
            les profondeurs inaccessibles de l'océan. Des découvertes incroyables sur de nouvelles espèces marines
            et des écosystèmes profonds continuent de surprendre le monde.
          </p>
        </section>
      </main>

      <footer className="ocean-footer">
        <p>&copy; 2024 L'Océan Mystérieux. Tous droits réservés.</p>
      </footer>
    </div>
  );
};

export default OceanPage;
