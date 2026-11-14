
"use strict";

const usersBody = document.getElementById("users-body");
const userCount = document.getElementById("user-count");
const search = document.getElementById("user-search");
const listMessage = document.getElementById("list-message");

const createSection = document.getElementById("create-section");
const createForm = document.getElementById("create-user-form");
const formError = document.getElementById("form-error");
const saveButton = document.getElementById("save-user");

const toggleForm = document.getElementById("toggle-form");
const cancelForm = document.getElementById("cancel-form");

let users = [];
let currentUserId = null;

const roleLabels = {
    ADMIN: "Admin",
    PROJECT_MANAGER: "Project Manager",
    DEVELOPER: "Developer",
    TESTER: "Tester",
    REPORTER: "Reporter"
};

function showError(message) {
    formError.textContent = message;
    formError.hidden = !message;
}

function showListMessage(message) {
    listMessage.textContent = message;
}

function renderUsers() {
    const term = search.value.trim().toLowerCase();

    const filtered = users.filter(user => {
        return `${user.name} ${user.email} ${user.role}`
            .toLowerCase()
            .includes(term);
    });

    usersBody.replaceChildren();

    userCount.textContent = `${filtered.length} usuário(s)`;

    if (filtered.length === 0) {
        showListMessage("Nenhum usuário encontrado.");
        return;
    }

    showListMessage("");

    for (const user of filtered) {
        const row = document.createElement("tr");

        const name = document.createElement("td");
        name.textContent = user.name;

        const email = document.createElement("td");
        email.textContent = user.email;

        const role = document.createElement("td");
        role.textContent = roleLabels[user.role] || user.role;

        const actions = document.createElement("td");

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.className = "delete-user";
        deleteButton.textContent = "Excluir";

        if (user.id === currentUserId) {
            deleteButton.disabled = true;
            deleteButton.title = "Você não pode excluir sua própria conta.";
        }

        deleteButton.addEventListener("click", () => {
            deleteUser(user);
        });

        actions.appendChild(deleteButton);
        row.append(name, email, role, actions);
        usersBody.appendChild(row);
    }
}

async function loadUsers() {
    const response = await fetch("/api/users");

    if (response.status === 401) {
        window.location.assign("/");
        return;
    }

    if (!response.ok) {
        throw new Error(`Erro HTTP ${response.status}`);
    }

    users = await response.json();
    renderUsers();
}

async function deleteUser(user) {
    const confirmed = window.confirm(
        `Deseja realmente excluir "${user.name}"?\n\n` +
        "Esta ação não poderá ser desfeita."
    );

    if (!confirmed) return;

    try {
        const response = await fetch(`/api/users/${user.id}`, {
            method: "DELETE"
        });

        if (response.status === 403) {
            window.alert(
                "Você não tem permissão para excluir este usuário."
            );
            return;
        }

        if (response.status === 409) {
            window.alert(
                "Este usuário possui tickets atribuídos. " +
                "Reatribua os tickets antes de excluí-lo."
            );
            return;
        }

        if (response.status === 404) {
            window.alert("Usuário não encontrado.");
            await loadUsers();
            return;
        }

        if (!response.ok) {
            throw new Error(`Erro HTTP ${response.status}`);
        }

        users = users.filter(item => item.id !== user.id);
        renderUsers();

    } catch (error) {
        console.error(error);
        window.alert("Não foi possível excluir o usuário.");
    }
}

function closeForm() {
    createSection.hidden = true;
    createForm.reset();
    showError("");
}

toggleForm.addEventListener("click", () => {
    createSection.hidden = false;
    showError("");
    document.getElementById("user-name").focus();
});

cancelForm.addEventListener("click", closeForm);

search.addEventListener("input", renderUsers);

createForm.addEventListener("submit", async event => {
    event.preventDefault();
    showError("");

    if (!createForm.reportValidity()) return;

    const payload = {
        name: document.getElementById("user-name").value.trim(),
        email: document.getElementById("user-email").value.trim(),
        password: document.getElementById("user-password").value,
        role: document.getElementById("user-role").value
    };

    saveButton.disabled = true;

    try {
        const response = await fetch("/api/users", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify(payload)
        });

        if (response.status === 409) {
            showError("Este e-mail já está cadastrado.");
            return;
        }

        if (response.status === 403) {
            showError("Você não tem permissão para criar usuários.");
            return;
        }

        if (response.status === 400) {
            showError("Verifique os campos informados.");
            return;
        }

        if (!response.ok) {
            throw new Error(`Erro HTTP ${response.status}`);
        }

        closeForm();
        await loadUsers();

    } catch (error) {
        console.error(error);
        showError("Não foi possível criar o usuário.");

    } finally {
        saveButton.disabled = false;
    }
});

async function initialize() {
    try {
        // A sessão informa quem está autenticado.
        const profileResponse = await fetch("/api/users/me");

        if (profileResponse.ok) {
            const profile = await profileResponse.json();

            // Identifica a conta atual pela correspondência do e-mail.
            // O próprio backend também bloqueia a autoexclusão.
            await loadUsers();

            currentUserId = users.find(
                user => user.email === profile.email
            )?.id ?? null;

            renderUsers();
        } else {
            await loadUsers();
        }

    } catch (error) {
        console.error(error);
        showListMessage("Não foi possível carregar os usuários.");
    }
}

initialize();
