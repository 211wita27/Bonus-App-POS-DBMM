document.addEventListener("DOMContentLoaded", () => {
  const revealItems = document.querySelectorAll(".reveal");
  revealItems.forEach((item, index) => {
    const delay = 80 * index;
    window.setTimeout(() => item.classList.add("is-visible"), delay);
  });

  document.querySelectorAll("form[data-loading]").forEach((form) => {
    form.addEventListener("submit", () => {
      form.classList.add("is-loading");
      form.querySelectorAll("button[type='submit']").forEach((btn) => {
        btn.disabled = true;
      });
    });
  });

  const pointsHost = document.querySelector("[data-current-points]");
  const currentPoints = pointsHost ? Number(pointsHost.getAttribute("data-current-points")) : null;
  if (!Number.isNaN(currentPoints)) {
    document.querySelectorAll("[data-reward-card]").forEach((card) => {
      const costInput = card.querySelector("[data-cost-input]");
      const submit = card.querySelector("button[type='submit']");
      if (!costInput || !submit) {
        return;
      }
      const updateState = () => {
        const cost = Number(costInput.value || 0);
        submit.disabled = cost > currentPoints;
      };
      costInput.addEventListener("input", updateState);
      updateState();
    });
  }
});
