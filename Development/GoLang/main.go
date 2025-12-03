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
