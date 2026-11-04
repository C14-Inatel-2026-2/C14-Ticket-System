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

async function loadActivities() {
    timeline.replaceChildren();

    try {
        const [commentsResponse, historyResponse] = await Promise.all([
            fetch(`/api/tickets/${currentTicketId}/comments`),
            fetch(`/api/tickets/${currentTicketId}/history`)
        ]);

        if (!commentsResponse.ok || !historyResponse.ok) {
            throw new Error("Não foi possível carregar as atividades.");
        }

        const comments = await commentsResponse.json();
        const history = await historyResponse.json();

        const activities = [
            ...comments.map(comment => ({
                type: "comment",
                author: comment.author,
                content: comment.content,
                createdAt: comment.createdAt
            })),

            ...history.map(entry => ({
                type: "history",
                author: "Sistema",
                content: `Alterou o status de ${
                    translateStatus(entry.previousStatus)
                } para ${translateStatus(entry.newStatus)}.`,
                createdAt: entry.changedAt
            }))
        ];

        activities.sort(
            (a, b) => new Date(a.createdAt) - new Date(b.createdAt)
        );

        if (activities.length === 0) {
            const empty = document.createElement("p");
            empty.className = "activity-empty";
            empty.textContent = "Nenhuma atividade registrada neste ticket.";
            timeline.appendChild(empty);
            return;
        }

        activities.forEach(activity => {
            timeline.appendChild(createActivityElement(activity));
        });

    } catch (error) {
        console.error(error);

        const message = document.createElement("p");
        message.className = "activity-error";
        message.textContent = "Erro ao carregar comentários e histórico.";

        timeline.appendChild(message);
    }
}

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
        commentFeedback.textContent = "Comentário publicado com sucesso!";

        await loadActivities();

    } catch (error) {
        console.error(error);
        commentFeedback.textContent = "Não foi possível publicar o comentário.";
    } finally {
        commentSubmit.disabled = false;
        commentSubmit.textContent = "Publicar comentário";
    }
});

loadActivities();