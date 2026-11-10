const timeline = document.getElementById("ticket-timeline");
const commentForm = document.getElementById("comment-form");
const commentInput = document.getElementById("comment-content");
const commentFeedback = document.getElementById("comment-feedback");
const commentSubmit = document.getElementById("comment-submit");

const currentTicketId = window.location.pathname
    .split("/")
    .filter(Boolean)
    .pop();

function formatDate(date) {
    return new Date(date).toLocaleString("pt-BR", {
        dateStyle: "short",
        timeStyle: "short"
    });
}

function translateStatus(status) {
    const labels = {
        OPEN: "Aberto",
        IN_PROGRESS: "Em andamento",
        RESOLVED: "Resolvido",
        CLOSED: "Encerrado"
    };

    return labels[status] || status;
}

function createActivityElement(activity) {
    const item = document.createElement("article");
    item.className = `activity-item ${activity.type}`;

    const marker = document.createElement("div");
    marker.className = "activity-marker";
    marker.textContent = activity.type === "comment" ? "💬" : "↻";

    const body = document.createElement("div");
    body.className = "activity-body";

    const heading = document.createElement("div");
    heading.className = "activity-item-header";

    const author = document.createElement("strong");
    author.textContent = activity.author;

    const date = document.createElement("time");
    date.dateTime = activity.createdAt;
    date.textContent = formatDate(activity.createdAt);

    heading.append(author, date);

    const content = document.createElement("p");
    content.textContent = activity.content;

    body.append(heading, content);
    item.append(marker, body);

    return item;
}

function translatePriority(priority) {
    const labels = {
        LOW: "Baixa",
        MEDIUM: "Média",
        HIGH: "Alta",
        URGENT: "Crítica"
    };

    return labels[priority] || priority;
}

function describeActivity(activity) {
    switch (activity.activityType) {
        case "STATUS_CHANGED":
            return `Alterou o status de ${
                translateStatus(activity.oldValue)
            } para ${translateStatus(activity.newValue)}.`;

        case "PRIORITY_CHANGED":
            return `Alterou a prioridade de ${
                translatePriority(activity.oldValue)
            } para ${translatePriority(activity.newValue)}.`;

        case "ASSIGNEE_CHANGED":
            return `Alterou o responsável de ${
                activity.oldValue
            } para ${activity.newValue}.`;

        default:
            return "Atualizou o ticket.";
    }
}

async function loadActivities() {
    timeline.replaceChildren();

    try {
        const response = await fetch(
            `/api/tickets/${currentTicketId}/activities`
        );

        if (!response.ok) {
            throw new Error("Erro ao carregar atividades");
        }

        const data = await response.json();

        if (data.length === 0) {
            const empty = document.createElement("p");
            empty.className = "activity-empty";
            empty.textContent = "Nenhuma atividade registrada.";
            timeline.appendChild(empty);
            return;
        }

        data.forEach(entry => {
            const activity = {
                type: entry.type,
                author: entry.author,
                createdAt: entry.createdAt,
                content: entry.type === "comment"
                    ? entry.content
                    : describeActivity(entry)
            };

            timeline.appendChild(createActivityElement(activity));
        });

    } catch (error) {
        console.error(error);

        const message = document.createElement("p");
        message.className = "activity-error";
        message.textContent = "Erro ao carregar atividades.";

        timeline.appendChild(message);
    }
}

loadActivities()

commentForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const content = commentInput.value.trim();

    if (!content) {
        commentFeedback.textContent = "Digite um comentário.";
        return;
    }

    commentSubmit.disabled = true;
    commentSubmit.textContent = "Publicando...";
    commentFeedback.textContent = "";

    try {
        const response = await fetch(
            `/api/tickets/${currentTicketId}/comments`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ content })
            }
        );

        if (!response.ok) {
            throw new Error(`Erro HTTP ${response.status}`);
        }

        commentInput.value = "";
        commentFeedback.textContent =
            "Comentário publicado com sucesso!";

        // Atualiza o histórico sem recarregar a página
        await loadActivities();

    } catch (error) {
        console.error("Erro ao publicar comentário:", error);

        commentFeedback.textContent =
            "Não foi possível publicar o comentário.";

    } finally {
        commentSubmit.disabled = false;
        commentSubmit.textContent = "Publicar comentário";
    }
});