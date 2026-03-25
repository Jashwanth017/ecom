function PlaceholderPage({ title, description }) {
  return (
    <section className="placeholder-card">
      <p className="eyebrow">Route Ready</p>
      <h2>{title}</h2>
      <p>{description}</p>
    </section>
  );
}

export default PlaceholderPage;
