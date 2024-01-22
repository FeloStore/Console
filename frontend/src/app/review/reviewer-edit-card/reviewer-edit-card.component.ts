// Copyright 2023 Logan Magee
//
// SPDX-License-Identifier: AGPL-3.0-only

import { NgIf } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

import { Edit } from '../../edit';

@Component({
    selector: 'app-reviewer-edit-card',
    standalone: true,
    imports: [MatButtonModule, MatCardModule, NgIf],
    templateUrl: './reviewer-edit-card.component.html',
})
export class ReviewerEditCardComponent {
    @Input({ required: true }) edit!: Edit;
    @Output() postReview = new EventEmitter<string>();

    onPostReview(): void {
        this.postReview.emit(this.edit.id);
    }
}
