
"use strict";

const form = document.getElementById("project-form");
const nameInput = document.getElementById("project-name");
const descriptionInput = document.getElementById("project-description");
const userSearch = document.getElementById("user-search");
const searchResults = document.getElementById("user-search-results");
const selectedUsersContainer = document.getElementById("selected-users");
const membersCount = document.getElementById("members-count");
const saveButton = document.getElementById("save-button");
const errorMessage = document.getElementById("form-error");
const formTitle = document.getElementById("form-title");

const editMatch = window.location.pathname.match(
    /^\/project\/(\d+)\/edit\/?$/
);

const projectId = editMatch ? Number(editMatch[1]) : null;
const isEditing = projectId !== null;

let availableUsers = [];
const selectedUserIds = new Set();

function showError(message) {
    errorMessage.textContent = message;
    errorMessage.hidden = !message;
}

async function getJson(url) {
    const response = await fetch(url, {
        headers: {
            "Accept": "application/json"
        }
    });

    if (!response.ok) {
        throw new Error(
            `Erro ao carregar dados (HTTP ${response.status}).`
        );
    }

    return response.json();
}

function findUser(userId) {
    return availableUsers.find(
        user => Number(user.id) === userId
    );
}

function renderSelectedUsers() {
    selectedUsersContainer.replaceChildren();

    membersCount.textContent =
        `${selectedUserIds.size} participante(s)`;

    if (selectedUserIds.size === 0) {
        const empty = document.createElement("p");
        empty.className = "selected-users-empty";
        empty.textContent = "Nenhum participante selecionado.";

        selectedUsersContainer.appendChild(empty);
        return;
    }

    const selected = [...selectedUserIds]
        .map(findUser)
        .filter(Boolean)
        .sort((a, b) =>
            a.name.localeCompare(b.name, "pt-BR")
        );

    for (const user of selected) {
        const row = document.createElement("div");
        row.className = "selected-user";

        const identity = document.createElement("div");
        identity.className = "selected-user-identity";

        const name = document.createElement("span");
        name.className = "selected-user-name";
        name.textContent = user.name;

        const email = document.createElement("span");
        email.className = "selected-user-email";
        email.textContent = user.email;

        identity.append(name, email);

        const removeButton = document.createElement("button");
        removeButton.type = "button";
        removeButton.className = "remove-user-button";
        removeButton.textContent = "×";

        removeButton.setAttribute(
            "aria-label",
            `Remover ${user.name}`
        );

        removeButton.addEventListener("click", () => {
            selectedUserIds.delete(Number(user.id));

            renderSelectedUsers();
            renderSearchResults();
        });

        row.append(identity, removeButton);
        selectedUsersContainer.appendChild(row);
    }
}

function renderSearchResults() {
    const term = userSearch.value
        .trim()
        .toLocaleLowerCase("pt-BR");

    searchResults.replaceChildren();

    if (!term) {
        searchResults.hidden = true;
        return;
    }

    const filtered = availableUsers.filter(user => {
        const searchable = `${user.name} ${user.email}`
            .toLocaleLowerCase("pt-BR");

        return searchable.includes(term)
            && !selectedUserIds.has(Number(user.id));
    });

    searchResults.hidden = false;

    if (filtered.length === 0) {
        const empty = document.createElement("p");
        empty.className = "search-empty";
        empty.textContent = "Nenhum usuário encontrado.";

        searchResults.appendChild(empty);
        return;
    }

    for (const user of filtered) {
        const option = document.createElement("button");
        option.type = "button";
        option.className = "user-search-option";

        const name = document.createElement("strong");
        name.textContent = user.name;

        const email = document.createElement("small");
        email.textContent = user.email;

        option.append(name, email);

        option.addEventListener("click", () => {
            selectedUserIds.add(Number(user.id));

            userSearch.value = "";

            renderSelectedUsers();
            renderSearchResults();

            userSearch.focus();
        });

        searchResults.appendChild(option);
    }
}

async function initializeForm() {
    saveButton.disabled = true;

    try {
        const users = await getJson("/api/users");

        if (!Array.isArray(users)) {
            throw new Error(
                "Resposta inesperada ao carregar usuários."
            );
        }

        availableUsers = users;

        if (isEditing) {
            formTitle.textContent = "Editar projeto";
            saveButton.textContent = "Salvar alterações";

            const [project, memberIds] = await Promise.all([
                getJson(`/api/projects/${projectId}`),
                getJson(`/api/projects/${projectId}/members`)
            ]);

            if (!Array.isArray(memberIds)) {
                throw new Error(
                    "Resposta inesperada ao carregar participantes."
                );
            }

            nameInput.value = project.name ?? "";
            descriptionInput.value = project.description ?? "";

            for (const id of memberIds) {
                const numericId = Number(id);

                if (
                    Number.isSafeInteger(numericId)
                    && findUser(numericId)
                ) {
                    selectedUserIds.add(numericId);
                }
            }
        }

        renderSelectedUsers();
        renderSearchResults();

        saveButton.disabled = false;

    } catch (error) {
        console.error(
            "Erro ao inicializar formulário:",
            error
        );

        showError(
            "Não foi possível carregar os dados do formulário."
        );
    }
}

// Pesquisa de usuários
userSearch.addEventListener("input", renderSearchResults);

userSearch.addEventListener("focus", renderSearchResults);

// Fechar sugestões ao clicar fora da pesquisa
document.addEventListener("click", event => {
    if (!event.target.closest(".user-search-container")) {
        searchResults.hidden = true;
    }
});

// Fechar sugestões com Escape
userSearch.addEventListener("keydown", event => {
    if (event.key === "Escape") {
        searchResults.hidden = true;
    }
});

// Criar ou editar projeto
form.addEventListener("submit", async event => {
    event.preventDefault();

    showError("");

    if (!form.reportValidity()) {
        return;
    }

    const payload = {
        name: nameInput.value.trim(),
        description: descriptionInput.value.trim(),
        membersIds: [...selectedUserIds]
    };

    if (!payload.name) {
        showError("Informe o nome do projeto.");
        nameInput.focus();
        return;
    }

    const url = isEditing
        ? `/api/projects/${projectId}`
        : "/api/projects";

    const method = isEditing ? "PUT" : "POST";

    saveButton.disabled = true;

    try {
        const response = await fetch(url, {
            method,
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const rawBody = await response.text();
            let body = null;

            try {
                body = JSON.parse(rawBody);
            } catch {
                // O servidor pode retornar erro sem JSON.
            }

            console.error("Erro ao salvar projeto:", {
                status: response.status,
                body: body ?? rawBody
            });

            let message = body?.detail || body?.message;

            if (
                body?.errors
                && typeof body.errors === "object"
            ) {
                const validationMessage = Object.values(
                    body.errors
                ).join("; ");

                message = validationMessage || message;
            }

            throw new Error(
                message ||
                `Não foi possível salvar o projeto (HTTP ${response.status}).`
            );
        }

        window.location.assign("/project");

    } catch (error) {
        console.error(error);

        showError(
            error.message ||
            "Não foi possível salvar o projeto."
        );

        saveButton.disabled = false;
    }
});

initializeForm();
