
const cy = cytoscape({
    container: document.getElementById("transactionGraph"),

    elements: [
        // Simulated accounts
        {
            data: {
                id: "A",
                label: "A\nbKash",
                provider: "bKash"
            }
        },
        {
            data: {
                id: "B",
                label: "B\nNagad",
                provider: "Nagad"
            }
        },
        {
            data: {
                id: "C",
                label: "C\nRocket",
                provider: "Rocket"
            }
        },
        {
            data: {
                id: "D",
                label: "D\nbKash",
                provider: "bKash"
            }
        },

        // Simulated transactions
        {
            data: {
                id: "T001",
                source: "A",
                target: "B",
                amount: 10000,
                time: "10:00"
            }
        },
        {
            data: {
                id: "T002",
                source: "B",
                target: "C",
                amount: 9800,
                time: "10:05"
            }
        },
        {
            data: {
                id: "T003",
                source: "C",
                target: "A",
                amount: 9500,
                time: "10:12"
            }
        },
        {
            data: {
                id: "T004",
                source: "A",
                target: "D",
                amount: 3000,
                time: "10:20"
            }
        }
    ],

    style: [
        // Default account styling
        {
            selector: "node",
            style: {
                "label": "data(label)",
                "background-color": "#d8fbe6",
                "border-color": "#35bb83",
                "border-width": 3,
                "color": "#391467",
                "font-size": 13,
                "font-weight": "bold",
                "text-valign": "center",
                "text-halign": "center",
                "text-wrap": "wrap",
                "width": 85,
                "height": 85
            }
        },

        // bKash accounts
        {
            selector: 'node[provider = "bKash"]',
            style: {
                "background-color": "#FFE1F0",
                "border-color": "#E92D85",
                "color": "#A61C60"
            }
        },

        // Nagad accounts
        {
            selector: 'node[provider = "Nagad"]',
            style: {
                "background-color": "#FFF0DD",
                "border-color": "#F28C28",
                "color": "#A65112"
            }
        },

        // Rocket accounts
        {
            selector: 'node[provider = "Rocket"]',
            style: {
                "background-color": "#EBDFFF",
                "border-color": "#8645C9",
                "color": "#542084"
            }
        },

        // Transaction connections
        {
            selector: "edge",
            style: {
                "label": "data(id)",
                "width": 3,
                "line-color": "#9651d5",
                "target-arrow-color": "#9651d5",
                "target-arrow-shape": "triangle",
                "curve-style": "bezier",
                "font-size": 12,
                "color": "#542084"
            }
        },

        // Selected account or transaction
        {
            selector: ":selected",
            style: {
                "border-color": "#d62991",
                "border-width": 5,
                "line-color": "#d62991",
                "target-arrow-color": "#d62991"
            }
        }
    ],

    layout: {
        name: "circle",
        padding: 60
    }
});

const details = document.getElementById("graphDetails");

// Display selected account details
cy.on("tap", "node", function(event) {
    const account = event.target.data();

    details.textContent =
        "Account: " + account.id +
        " | Provider: " + account.provider;
});

// Display selected transaction details
cy.on("tap", "edge", function(event) {
    const transaction = event.target.data();

    details.textContent =
        "Transaction: " + transaction.id +
        " | From: " + transaction.source +
        " | To: " + transaction.target +
        " | Amount: Tk " + transaction.amount +
        " | Time: " + transaction.time;
});
