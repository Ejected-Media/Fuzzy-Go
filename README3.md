This is great. Ejected Media is building a solid portfolio of tools now.
Since the repository is live, let's make the Initial Commit count. We need to lay down the Go Data Structures that match the Firestore Schema we discussed. This will serve as the "contract" between your Go backend and the Firestore database.
Here is the recommended folder structure and the code to get Fuzzy-Go initialized.

1. Initialize the Module
Open your terminal in the project folder and run:
go mod init github.com/Ejected-Media/Fuzzy-Go
go get firebase.google.com/go
go get google.golang.org/api/option

2. Create the Data Models
Create a file at internal/model/types.go.
This maps our "Projects -> Lanes -> Cards" hierarchy to Go structs with Firestore tags.

```
package model

import (
	"time"
)

// Project represents the top-level board (e.g., "Fuzzy-Go Mobile App")
type Project struct {
	ID         string    `firestore:"-" json:"id"` // ID is handled by the doc ref, not the body
	Name       string    `firestore:"name" json:"name"`
	OwnerID    string    `firestore:"owner_id" json:"owner_id"`
	CreatedAt  time.Time `firestore:"created_at" json:"created_at"`
	ThemeColor string    `firestore:"theme_color" json:"theme_color"` // Hex code
}

// Lane represents a column (e.g., "Backlog", "In Review")
type Lane struct {
	ID         string `firestore:"-" json:"id"`
	ProjectID  string `firestore:"project_id" json:"project_id"`
	Title      string `firestore:"title" json:"title"`
	OrderIndex int    `firestore:"order_index" json:"order_index"` // 0, 1, 2 for sorting
}

// Card represents a single issue or task
type Card struct {
	ID          string    `firestore:"-" json:"id"`
	ProjectID   string    `firestore:"project_id" json:"project_id"` // Root level query support
	LaneID      string    `firestore:"lane_id" json:"lane_id"`       // Which column is it in?
	Title       string    `firestore:"title" json:"title"`
	Description string    `firestore:"description" json:"description"`
	Priority    string    `firestore:"priority" json:"priority"` // "Low", "Fuzzy", "Critical"
	Tags        []string  `firestore:"tags" json:"tags"`
	AssignedTo  []string  `firestore:"assigned_to" json:"assigned_to"` // User UIDs
	CreatedAt   time.Time `firestore:"created_at" json:"created_at"`
}
```

3. Create the Server Skeleton
Create a file at cmd/server/main.go.
We will keep this simple for now: just connecting to Firestore to prove the configuration works.

```
package main

import (
	"context"
	"fmt"
	"log"
	"os"

	firebase "firebase.google.com/go"
	"google.golang.org/api/option"
)

func main() {
	fmt.Println("Starting Fuzzy-Go Backend...")

	// 1. Initialize Firebase/Firestore
	ctx := context.Background()
	
	// We will use a service account key later. 
	// For now, checks if we can init the app wrapper.
	opt := option.WithCredentialsFile("serviceAccountKey.json")
	app, err := firebase.NewApp(ctx, nil, opt)
	if err != nil {
		log.Printf("Warning: Firebase init failed (expected if no key yet): %v\n", err)
	} else {
		fmt.Println("Firebase App Initialized successfully.")
	}

	// 2. Placeholder for HTTP Server
	// We will likely use 'net/http' or 'chi' or 'gin' here later.
	client, err := app.Firestore(ctx)
	if err != nil {
		log.Printf("Warning: Could not get Firestore client: %v\n", err)
	} else {
		defer client.Close()
		fmt.Println("Firestore Client Connected.")
	}
	
	// Keep process alive for now (mock server)
	fmt.Println("Server ready on port 8080 (Placeholder)")
}
```

Immediate Next Step

We need to get your Firebase Service Account Key so the Go code can actually talk to Google Cloud.
 * Go to the Firebase Console.
 * Create a project named "Fuzzy-Go".
 * Go to Project Settings -> Service Accounts.
 * Click Generate New Private Key.
 * Save that file as serviceAccountKey.json in the root of your Go project (and add it to your .gitignore immediately so you don't leak it to GitHub!).
Would you like me to write a .gitignore file for you next, or do you want to jump straight into the Android client setup?
